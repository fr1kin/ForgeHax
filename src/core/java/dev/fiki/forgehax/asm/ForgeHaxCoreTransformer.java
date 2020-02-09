package dev.fiki.forgehax.asm;

import cpw.mods.modlauncher.api.*;
import dev.fiki.forgehax.asm.patches.*;
import dev.fiki.forgehax.asm.utils.transforming.ClassTransformer;
import dev.fiki.forgehax.common.LoggerProvider;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.MethodNode;

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
  public void beginScanning(IEnvironment environment) {}

  @Override
  public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {
    if(otherServices.stream()
        .map(String::toLowerCase)
        .anyMatch(str -> str.contains("mixin"))) {
      logger.warn("ForgeHaxCore found Mixin. Some patches may not apply.");
    }
  }

  @Nonnull
  @Override
  @Deprecated
  public List<ITransformer> transformers() {
    return Stream.of(
        new BlockPatch(),
        new BoatEntityPatch(),
        new LivingEntityPatch(),
        new EntityPatch(),
        new EntityPlayerSPPatch(),
        new EntityRendererPatch(),
        new GameRendererPatch(),
        new KeyBindingPatch(),
        new MinecraftPatch(),
        new NetManagerPatch(),
        new PlayerControllerPatch(),
        new PlayerTabOverlayPatch(),
        new BoatRendererPatch(),
        new WorldRendererPatch(),
        new VisGraphPatch(),
        new WorldPatch())
        // this map below can be commented out to use classtransformers instead
        .map(ClassTransformer::getMethodTransformers)
        .flatMap(List::stream)
        .filter(mt -> mt.getMethod().getSrg() != null) // temporary
        // cpws code doesn't check super classes, so we have to wrap our instance
        .map(mt -> new ITransformer<MethodNode>() {
          @Nonnull
          @Override
          public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
            return mt.transform(input, context);
          }

          @Nonnull
          @Override
          public TransformerVoteResult castVote(ITransformerVotingContext context) {
            return mt.castVote(context);
          }

          @Nonnull
          @Override
          public Set<Target> targets() {
            return mt.targets();
          }
        })
        .collect(Collectors.toList());
  }
}
