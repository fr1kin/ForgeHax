package com.matt.forgehax.asm;

import com.matt.forgehax.asm.TypesMc.Classes;
import com.matt.forgehax.asm.patches.*;
import com.matt.forgehax.asm.patches.special.*;
import com.matt.forgehax.asm.utils.ASMHelper;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.AsmPattern;
import com.matt.forgehax.asm.utils.InsnPattern;
import com.matt.forgehax.asm.utils.asmtype.ASMField;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMFieldBuilder;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMMethodBuilder;
import com.matt.forgehax.asm.utils.asmtype.builders.ParameterBuilder;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

@IFMLLoadingPlugin.SortingIndex
public class ForgeHaxTransformer implements IClassTransformer, ASMCommon {
  private HashMap<String, ClassTransformer> transformingClasses = new HashMap<>();
  private int transformingLevel = 0;

  @SuppressWarnings("ResultOfMethodCallIgnored")
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

    // stuff that needs to be pre-loaded
    Objects.nonNull(TypesMc.Classes.class);
    Objects.nonNull(TypesMc.Methods.class);
    Objects.nonNull(TypesMc.Fields.class);
    Objects.nonNull(TypesHook.Classes.class);
    Objects.nonNull(TypesHook.Methods.class);
    Objects.nonNull(TypesHook.Fields.class);
    Objects.nonNull(ASMMethodBuilder.class);
    Objects.nonNull(ASMFieldBuilder.class);
    Objects.nonNull(ParameterBuilder.class);
    Objects.nonNull(GenericFutureListener.class);
    Objects.nonNull(ChannelHandlerContext.class);
    Objects.nonNull(ChannelOutboundInvoker.class);
    Objects.nonNull(ASMHelper.class);
    Objects.nonNull(ASMField.class);
    Objects.nonNull(AsmPattern.Builder.class);
    Objects.nonNull(AsmPattern.class);
    Objects.nonNull(InsnPattern.class);
    Objects.nonNull(ASMStackLogger.class);
  }

  private void registerTransformer(ClassTransformer transformer) {
    transformingClasses.put(transformer.getTransformingClassName(), transformer);
  }

  @Override
  public byte[] transform(String name, String realName, byte[] bytes) {
    if (transformingLevel > 0)
      LOGGER.warn("Reentrant class loaded {} (level={})", realName, transformingLevel);

    ++transformingLevel;
    if (transformingClasses.containsKey(realName)) {
      ClassTransformer transformer = transformingClasses.get(realName);
      try {
        LOGGER.info("Transforming class " + realName);

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        transformer.transform(classNode);

        ClassWriter classWriter =
            new NoClassLoadingClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        classNode.accept(classWriter);

        // let gc clean this up
        transformingClasses.remove(realName);

        bytes = classWriter.toByteArray();
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

    --transformingLevel;
    return bytes;
  }

  private class NoClassLoadingClassWriter extends ClassWriter {
    NoClassLoadingClassWriter(int flags) {
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
