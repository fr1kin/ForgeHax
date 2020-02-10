package dev.fiki.forgehax.asm;

import cpw.mods.modlauncher.api.*;
import dev.fiki.forgehax.asm.patches.*;
import dev.fiki.forgehax.asm.utils.transforming.RegisterTransformer;
import dev.fiki.forgehax.asm.utils.transforming.Wrappers;
import dev.fiki.forgehax.common.LoggerProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class ForgeHaxCoreTransformer implements ITransformationService {
  @Getter
  static Logger logger = null;

  @Nonnull
  @Override
  public String name() {
    return "ForgeHaxCore";
  }

  @Override
  public void initialize(IEnvironment environment) {
    logger = LoggerProvider.builder()
        .contextClass(ForgeHaxCoreTransformer.class)
        .label("core")
        .build()
        .getLogger();

    logger.info("ForgeHaxCore initializing");
  }

  @Override
  public void beginScanning(IEnvironment environment) {
  }

  @Override
  public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
    if (otherServices.stream()
        .map(String::toLowerCase)
        .anyMatch(str -> str.contains("mixin"))) {
      logger.warn("ForgeHaxCore found Mixin. Some patches may not apply.");
    }
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
        KeyBindingPatch.class,
        MinecraftPatch.class,
        NetManagerPatch.class,
        PlayerControllerPatch.class,
        PlayerTabOverlayPatch.class,
        BoatRendererPatch.class,
        WorldRendererPatch.class,
        VisGraphPatch.class,
        WorldPatch.class
    );
  }

  @SuppressWarnings("unchecked")
  private List<ITransformer> getTransformersForClasses(Class<?>... patches) {
    return (List<ITransformer>) Stream.of(patches) // epic cast because compiler bug??
        .flatMap(clazz -> Stream.concat(Stream.of(clazz), Stream.of(clazz.getDeclaredClasses())))
        .filter(clazz -> clazz.isAnnotationPresent(RegisterTransformer.class))
        .filter(ForgeHaxCoreTransformer::requiresZeroArgConstructor)
        .map(ForgeHaxCoreTransformer::newInstance)
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
      getLogger().warn("Class \"{}\" has no zero-argument constructor!", clazz.getSimpleName());
    }
    return false;
  }
}
