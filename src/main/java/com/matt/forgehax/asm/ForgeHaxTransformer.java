package com.matt.forgehax.asm;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.TypesMc.Classes;
import com.matt.forgehax.asm.patches.*;
import com.matt.forgehax.asm.patches.special.*;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import java.util.Map;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class ForgeHaxTransformer implements IClassTransformer, ASMCommon {
  private Map<String, ClassTransformer> transformingClasses = Maps.newHashMap();

  public ForgeHaxTransformer() {
    registerTransformer(new BlockPatch());
    registerTransformer(new ChunkRenderContainerPatch());
    registerTransformer(new ChunkRenderDispatcherPatch());
    registerTransformer(new ChunkRenderWorkerPatch());
    registerTransformer(new EntityLivingBasePatch());
    registerTransformer(new EntityPatch());
    registerTransformer(new EntityPlayerSPPatch());
    registerTransformer(new EntityRendererPatch());
    registerTransformer(new MinecraftPatch());
    registerTransformer(new NetManagerPatch());
    registerTransformer(new NetManager$4Patch());
    registerTransformer(new PlayerControllerMCPatch());
    registerTransformer(new RenderChunkPatch());
    registerTransformer(new RenderGlobalPatch());
    registerTransformer(new BufferBuilderPatch());
    registerTransformer(new VisGraphPatch());
    registerTransformer(new WorldPatch());

    // Babbaj
    registerTransformer(new BoatPatch());
    registerTransformer(new RenderBoatPatch());
    registerTransformer(new PlayerTabOverlayPatch());
    registerTransformer(new KeyBindingPatch());
    registerTransformer(new SchematicPrinterPatch());

    // special transformers
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

        ClassWriter classWriter =
            new DepozzedClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        classNode.accept(classWriter);

        // let gc clean this up
        transformingClasses.remove(realName);

        return classWriter.toByteArray();
      } catch (Exception e) {
        LOGGER.error(
            e.getClass().getSimpleName()
                + " thrown from transforming class "
                + realName
                + ": "
                + e.getMessage());
        ASMStackLogger.printStackTrace(e);
      }
    }
    return bytes;
  }

  private class DepozzedClassWriter extends ClassWriter {
    DepozzedClassWriter(int flags) {
      super(flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
      if (type1.matches(Classes.GuiMainMenu.getRuntimeInternalName()))
        return Classes.GuiScreen.getRuntimeInternalName(); // stupid edge case
      else return "java/lang/Object"; // credits to popbob
    }
  }
}
