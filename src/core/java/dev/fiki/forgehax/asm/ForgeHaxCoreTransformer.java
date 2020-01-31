package dev.fiki.forgehax.asm;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import dev.fiki.forgehax.asm.patches.*;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

public class ForgeHaxCoreTransformer implements ITransformationService {
  @Nonnull
  @Override
  public String name() {
    return "ForgeHaxCore";
  }

  @Override
  public void initialize(IEnvironment environment) {
    getLogger().info("ForgeHaxCore initializing");
  }

  @Override
  public void beginScanning(IEnvironment environment) {}

  @Override
  public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
    if(otherServices.stream()
        .map(String::toLowerCase)
        .anyMatch(str -> str.contains("mixin"))) {
      getLogger().warn("ForgeHaxCore found Mixin. Some patches may not apply.");
    }
  }

  @Nonnull
  @Override
  public List<ITransformer> transformers() {
    return Stream.of(
        new BlockPatch(),
        new BoatEntityPatch(),
        new LivingEntityPatch(),
        new EntityPatch(),
        new EntityPlayerSPPatch(),
        new EntityRendererPatch(),
        new KeyBindingPatch(),
        new MinecraftPatch(),
        new NetManagerPatch(),
        new PlayerControllerPatch(),
        new PlayerTabOverlayPatch(),
        new RenderBoatPatch(),
        new WorldRendererPatch(),
        new VisGraphPatch(),
        new WorldPatch())
        // this map below can be commented out to use classtransformers instead
        .map(ClassTransformer::getMethodTransformers)
        .map(ITransformer.class::cast)
        .collect(Collectors.toList());
  }
}
