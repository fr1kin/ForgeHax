package com.matt.forgehax.asm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm.helper.ClassTransformer;
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
public class ForgeHaxTransformer implements IClassTransformer {
    private Map<String, ClassTransformer> transformingClasses = Maps.newHashMap();

    public ForgeHaxTransformer() {
        // add patches to transformingClasses here
        transformingClasses.put("net.minecraft.world.World", new WorldPatch());
        transformingClasses.put("net.minecraft.entity.Entity", new EntityPatch());
        transformingClasses.put("net.minecraft.client.renderer.RenderGlobal", new RenderGlobalPatch());
        transformingClasses.put("net.minecraft.block.Block", new BlockPatch());
        transformingClasses.put("net.minecraft.client.renderer.VertexBuffer", new VertexBufferPatch());
        transformingClasses.put("net.minecraft.client.renderer.EntityRenderer", new EntityRendererPatch());
        transformingClasses.put("net.minecraft.network.NetworkManager", new NetManagerPatch());
        transformingClasses.put("net.minecraft.network.NetworkManager$4", new NetManager$4Patch());
        transformingClasses.put("net.minecraft.client.renderer.chunk.VisGraph", new VisGraphPatch());
        transformingClasses.put("net.minecraft.client.entity.EntityPlayerSP", new EntityPlayerSPPatch());
        transformingClasses.put("net.minecraft.client.renderer.BlockRendererDispatcher", new BlockRendererDispatcherPatch());
    }

    @Override
    public byte[] transform(String name, String realName, byte[] bytes) {
        if (transformingClasses.containsKey(realName)) {
            List<String> log = Lists.newArrayList();
            ClassTransformer transformer = transformingClasses.get(realName);
            try {
                ForgeHaxCoreMod.print("Transforming class %s (%s)\n", realName, name);
                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytes);
                classReader.accept(classNode, 0);

                transformer.setClassName(realName, name);
                transformer.transform(classNode);

                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(classWriter);

                ForgeHaxHooks.setHooksLog(realName, transformer.getErrorLog(), transformer.methodCount);

                // let gc clean this up
                transformingClasses.remove(realName);

                return classWriter.toByteArray();
            } catch (Exception e) {
                transformer.markUnsuccessful();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                transformer.log("TransformException", "%s\n", sw.toString());
            } finally {
                if(!transformer.isSuccessful()) {
                    // at the very top
                    log.add("####################\n");
                    log.add(String.format("ERROR TRANSFORMING CLASS '%s'\n", realName));
                    log.add(String.format("hasFoundAllMethods=%s\n", transformer.hasFoundAllMethods() ? "true" : "false"));
                    log.add(String.format("Found %d out of %d methods\n", transformer.foundMethods, transformer.methodCount));
                    log.add("Error log:");
                    // tab report
                    for(String msg : transformer.getErrorLog()) {
                        log.add("\t" + msg);
                    }
                    log.add("\n");
                    log.add("####################\n");
                    ForgeHaxCoreMod.print(log);
                } else {
                    ForgeHaxCoreMod.print("Successfully transformed class '%s'", realName);
                }
            }
        }
        return bytes;
    }
}
