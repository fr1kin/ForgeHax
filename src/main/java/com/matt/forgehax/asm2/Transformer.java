package com.matt.forgehax.asm2;

import com.fr1kin.asmhelper.detours.Detour;
import com.fr1kin.asmhelper.exceptions.DetourException;
import com.fr1kin.asmhelper.types.ASMClass;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm2.detours.ClassDetours;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Map;

/**
 * Created on 1/11/2017 by fr1kin
 */
public class Transformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(ClassDetours.containsClassName(transformedName)) {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            final ASMClass parentClass = ASMClass.getOrCreateClass(classNode);

            ClassDetours.lookupClassName(transformedName).getDetours().forEach(detour -> {
                try {
                    detour.apply(parentClass, classNode);
                } catch (DetourException e) {
                    CoreMod.getLogger().error(String.format(
                            "Error transforming target method '%s' with hook '%s': %s",
                            detour.getTargetMethod().toString(),
                            detour.getHookMethod().toString(),
                            e.getMessage()
                    ));
                }
            });

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);

            return classWriter.toByteArray();
        } else return basicClass;
    }
}
