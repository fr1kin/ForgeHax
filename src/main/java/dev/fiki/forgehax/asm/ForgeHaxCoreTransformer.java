package dev.fiki.forgehax.asm;

import com.google.common.collect.ImmutableList;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import dev.fiki.forgehax.api.log.ForgeHaxLog4J2Configuration;
import dev.fiki.forgehax.asm.patches.*;
import dev.fiki.forgehax.asm.utils.EZ;
import dev.fiki.forgehax.asm.utils.transforming.Patch;
import dev.fiki.forgehax.asm.utils.transforming.PatchScanner;
import dev.fiki.forgehax.asm.utils.transforming.Wrappers;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class ForgeHaxCoreTransformer implements ITransformationService {
  @Getter
  private List<String> otherServices = Collections.emptyList();

  public ForgeHaxCoreTransformer() {
    ForgeHaxLog4J2Configuration.create();
    log.info("ForgeHaxCore initializing");
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
    this.otherServices = ImmutableList.copyOf(otherServices);

    EZ.inject();

    if (System.getProperty("forgehax.classdump") != null) {
      EZ.enableClassDumping();
    }
  }

  @Nonnull
  @Override
  public List<ITransformer> transformers() {
    return System.getProperty("forgehax.disable-transformers") != null ? Collections.emptyList()
        : getTransformersForClasses(
        BlockModelRendererPatch.class,
        BoatEntityPatch.class,
        LivingEntityPatch.class,
        EntityPatch.class,
        ChunkRenderDispatcher$ChunkRender$RebuildTaskPatch.class,
        ChunkRenderDispatcher$ChunkRenderPatch.class,
        ClientEntityPlayerPatch.class,
        GameRendererPatch.class,
        IVertexBuilderPatch.class,
        MinecraftPatch.class,
        NetManagerPatch.class,
        PlayerControllerPatch.class,
        PlayerEntityPatch.class,
        PlayerTabOverlayPatch.class,
        BoatRendererPatch.class,
        ViewFrustumPatch.class,
        VisGraphPatch.class,
        WorldRendererPatch.class
    );
  }

  @SuppressWarnings("unchecked")
  private List<ITransformer> getTransformersForClasses(Class<?>... patches) {
    return (List<ITransformer>) Stream.of(patches) // epic cast because compiler bug??
        .filter(Patch.class::isAssignableFrom)
        .filter(ForgeHaxCoreTransformer::requiresZeroArgConstructor)
        .map(ForgeHaxCoreTransformer::newInstance)
        .map(Patch.class::cast)
        .map(this::newPatchScanner)
        .map(PatchScanner::getTransformers)
        .flatMap(List::stream)
        .map(ITransformer.class::cast)
        .map(Wrappers::createWrapper)
        .collect(Collectors.toList());
  }

  private PatchScanner newPatchScanner(Patch patch) {
    return new PatchScanner(this, patch);
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
      log.warn("Class \"{}\" has no zero-argument constructor!", clazz.getSimpleName());
    }
    return false;
  }
}
