package dev.fiki.forgehax.asm;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import dev.fiki.forgehax.asm.patches.*;
import dev.fiki.forgehax.asm.utils.EZ;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import dev.fiki.forgehax.asm.utils.transforming.PatchScanner;
import dev.fiki.forgehax.asm.utils.transforming.Wrappers;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForgeHaxCoreTransformer implements ITransformationService, ASMCommon {
  public ForgeHaxCoreTransformer() {
    LOGGER.info("ForgeHaxCore initializing");
  }

  @Nonnull
  @Override
  public String name() {
    return "ForgeHaxCore";
  }

  @Override
  public void initialize(IEnvironment environment) {
  }

  @Override
  public void beginScanning(IEnvironment environment) {
  }

  @Override
  public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
    if (otherServices.stream()
        .map(String::toLowerCase)
        .anyMatch(str -> str.contains("mixin"))) {
      LOGGER.warn("ForgeHaxCore found Mixin. Some patches may not apply.");
    }

    EZ.inject();
  }

  @Nonnull
  @Override
  public List<ITransformer> transformers() {
    return getTransformersForClasses(
        BlockPatch.class,
        BoatEntityPatch.class,
        LivingEntityPatch.class,
        EntityPatch.class,
        ClientEntityPlayerPatch.class,
        GameRendererPatch.class,
        MinecraftPatch.class,
        NetManagerPatch.class,
        PlayerControllerPatch.class,
        PlayerEntityPatch.class,
        PlayerTabOverlayPatch.class,
        BoatRendererPatch.class,
        WorldRendererPatch.class,
        VisGraphPatch.class
    );
  }

  @SuppressWarnings("unchecked")
  private List<ITransformer> getTransformersForClasses(Class<?>... patches) {
    return (List<ITransformer>) Stream.of(patches) // epic cast because compiler bug??
        .filter(Patch.class::isAssignableFrom)
        .filter(ForgeHaxCoreTransformer::requiresZeroArgConstructor)
        .map(ForgeHaxCoreTransformer::newInstance)
        .map(Patch.class::cast)
        .map(PatchScanner::new)
        .map(PatchScanner::getTransformers)
        .flatMap(List::stream)
        .map(ITransformer.class::cast)
        .map(Wrappers::createWrapper)
        .collect(Collectors.toList());
  }

  @SneakyThrows
  private static <T> T newInstance(Class<T> clazz) {
    Constructor<T> constructor = clazz.getDeclaredConstructor();
    constructor.setAccessible(true);
    return constructor.newInstance();
  }

  private static boolean requiresZeroArgConstructor(Class<?> clazz) {
    try {
      return clazz.getDeclaredConstructor() != null;
    } catch (NoSuchMethodException ex) {
      LOGGER.warn("Class \"{}\" has no zero-argument constructor!", clazz.getSimpleName());
    }
    return false;
  }
}
