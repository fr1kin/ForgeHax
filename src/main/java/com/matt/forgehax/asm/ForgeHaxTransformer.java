package com.matt.forgehax.asm;

import com.matt.forgehax.asm.TypesMc.Classes;
import com.matt.forgehax.asm.patches.*;
import com.matt.forgehax.asm.patches.special.*;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.transforming.ClassTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.core.util.Booleans;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

@IFMLLoadingPlugin.SortingIndex(1001)
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

    // exclude transformers from Mixin
    try {
      int count = addExcludedTransformersToMixin(ForgeHaxTransformer.class.getName());
      LOGGER.info("ForgeHax transformer exclusions successfully added into {} phases", count);
    } catch (MixinMissingException e) {
      LOGGER.info("Mixin not detected running, skipped adding transformer exclusions");
    } catch (NullPointerException
        | ClassNotFoundException
        | NoSuchFieldException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      LOGGER.info("Failed to add ForgeHax transformer exclusion into Mixin environment");
      ASMStackLogger.printStackTrace(e);
    }
  }

  private void registerTransformer(ClassTransformer transformer) {
    transformingClasses.put(transformer.getTransformingClassName(), transformer);
  }

  @Override
  public byte[] transform(String name, String realName, byte[] bytes) {
    if (transformingLevel > 0)
      LOGGER.warn("Reentrant class loaded {} at level {}", realName, transformingLevel);

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

  /**
   * This will prevent Mixin from feeding our transformer meta class data which it may later discard
   */
  private static int addExcludedTransformersToMixin(String... excludedTransformers)
      throws MixinMissingException, NullPointerException, ClassNotFoundException,
          NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
          InvocationTargetException {
    // get the MixinEnvironment class
    Class<?> class_MixinEnvironment;
    try {
      class_MixinEnvironment = Class.forName("org.spongepowered.asm.mixin.MixinEnvironment");
    } catch (ClassNotFoundException e) {
      throw new MixinMissingException();
    }

    int count = 0;

    Method method_addTransformerExclusion =
        class_MixinEnvironment.getDeclaredMethod("addTransformerExclusion", String.class);
    method_addTransformerExclusion.setAccessible(true);

    // get the environment phase subclass
    Class<?> class_MixinEnvironment$Phase =
        Class.forName("org.spongepowered.asm.mixin.MixinEnvironment$Phase");

    // get the phases field
    Field field_phases = class_MixinEnvironment$Phase.getDeclaredField("phases");
    field_phases.setAccessible(true);

    // get the getEnvironment method (non-static)
    Method method_getEnvironment = class_MixinEnvironment$Phase.getDeclaredMethod("getEnvironment");
    method_getEnvironment.setAccessible(true);

    // get the list of phases
    List<Object> phases = (List<Object>) field_phases.get(null);
    Objects.requireNonNull(phases, "phases instance is null!");

    for (Object phase : phases) {
      // get the environment variable
      Object instance;
      try {
        instance = method_getEnvironment.invoke(phase);
      } catch (IllegalArgumentException e) {
        // bad ordinal, skip this instance
        continue;
      }
      Objects.requireNonNull(instance, "MixinEnvironment$Phase::getEnvironment returned null!");

      for (String className : excludedTransformers) {
        method_addTransformerExclusion.invoke(instance, className);
        count++;
      }
    }

    return count; // number of successful class exclusions
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

  private static class MixinMissingException extends Exception {}
}
