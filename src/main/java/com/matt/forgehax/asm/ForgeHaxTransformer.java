package com.matt.forgehax.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm.helper.AsmStackLogger;
import com.matt.forgehax.asm.helper.transforming.ClassTransformer;
import com.matt.forgehax.asm.patches.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class ForgeHaxTransformer implements IClassTransformer, ASMCommon {
    private Map<String, ClassTransformer> transformingClasses = Maps.newHashMap();

    public ForgeHaxTransformer() {
        registerTransformer(new BlockPatch());
        registerTransformer(new BlockRendererDispatcherPatch());
        registerTransformer(new EntityPatch());
        registerTransformer(new EntityPlayerSPPatch());
        registerTransformer(new EntityRendererPatch());
        registerTransformer(new NetManagerPatch());
        registerTransformer(new NetManager$4Patch());
        registerTransformer(new RenderGlobalPatch());
        registerTransformer(new VertexBufferPatch());
        registerTransformer(new VisGraphPatch());
        registerTransformer(new WorldPatch());
    }

    private void registerTransformer(ClassTransformer transformer) {
        transformingClasses.put(transformer.getTransformingClassName(), transformer);
    }

    @Override
    public byte[] transform(String name, String realName, byte[] bytes) {
        if (transformingClasses.containsKey(realName)) {
            ClassTransformer transformer = transformingClasses.get(realName);
            try {
                LOGGER.info("Transforming class " + realName);

                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytes);
                classReader.accept(classNode, 0);

                transformer.transform(classNode);

                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);

                // let gc clean this up
                transformingClasses.remove(realName);

                return classWriter.toByteArray();
            } catch (Exception e) {
                LOGGER.error(e.getClass().getSimpleName() + " thrown from transforming class " + realName + ": " + e.getMessage());
                AsmStackLogger.printStackTrace(e);
            }
        }
        return bytes;
    }
}
