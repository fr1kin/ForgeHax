package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.exceptions.DetourException;
import com.fr1kin.asmhelper.types.ASMClass;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm2.detours.BlockDetours;
import com.matt.forgehax.asm2.detours.ClassDetour;
import com.matt.forgehax.asm2.detours.EntityRendererDetours;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.ASMifier;

import java.util.Map;

/**
 * Created on 1/11/2017 by fr1kin
 */
public class Transformer implements IClassTransformer {
    private final Map<String, ClassDetour> CLASS_DETOUR_MAP = Maps.newHashMap();

    public Transformer() {
        Core.Initialization.registerListener((obfuscated) -> {
            registerClass(new BlockDetours());
            registerClass(new EntityRendererDetours());
        });
    }

    private void registerClass(ClassDetour classDetour) {
        CLASS_DETOUR_MAP.put(classDetour.getDetouredClass().getClassName(), classDetour);
    }

    public boolean containsClassName(String className) {
        return CLASS_DETOUR_MAP.containsKey(className);
    }

    public ClassDetour lookupClassName(String className) {
        // TODO: am passing transformed name, need to either find the mcp name or just use the realtime name
        return CLASS_DETOUR_MAP.get(className);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(containsClassName(name)) {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            final ClassDetour classDetour = CLASS_DETOUR_MAP.get(transformedName);
            final ASMClass parentClass = classDetour.getDetouredClass();

            classDetour.getDetours().forEach(detour -> {
                try {
                    if(!detour.apply(parentClass, classNode)) {
                        Core.getLogger().warn(String.format(
                                "Problem transforming target method '%s' with hook '%s': Detour::apply returned false (method probably doesn't exist)",
                                detour.getTargetMethod().toString(),
                                detour.getHookMethod().toString()
                        ));
                    } else {
                        Core.getLogger().info(String.format(
                                "Successfully transformed target method '%s' with hook '%s'",
                                detour.getTargetMethod().toString(),
                                detour.getHookMethod().toString()
                        ));
                    }
                } catch (RuntimeException e) {
                    Core.getLogger().error(String.format(
                            "Error transforming target method '%s' with hook '%s': %s",
                            detour.getTargetMethod().toString(),
                            detour.getHookMethod().toString(),
                            e.getMessage()
                    ));
                    e.printStackTrace();
                }
            });

            try {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);
                return classWriter.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return basicClass;
    }
}
