package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMClass;

import java.lang.reflect.Constructor;
import java.util.*;

import dev.fiki.forgehax.common.asmtype.ASMMethod;
import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

import static dev.fiki.forgehax.asm.ASMCommon.*;

@Getter
public abstract class ClassTransformer implements ITransformer<ClassNode>, TypesMc, Opcodes, ASMHelper.MagicOpcodes {
  public abstract ASMClass getTransformingClass();

  public String getTransformingClassName() {
    return getTransformingClass().getClassName();
  }

  @Nonnull
  @Override
  public ClassNode transform(ClassNode node, ITransformerVotingContext ctx) {
    final String description = getClass().isAnnotationPresent(RegisterTransformer.class)
        ? getClass().getAnnotation(RegisterTransformer.class).value()
        : getClass().getSimpleName();

    getLogger().debug("Transforming class {}", getTransformingClassName());

    try {
      this.transform(node);
      getLogger().debug("Successfully transformed class \"{}\"", description);
    } catch (Throwable t) {
      // catch errors
      getLogger().error("Failed to transform task \"{}\" in class {}",
          description, getTransformingClassName());
      getLogger().error(t, t);
    }

    return node;
  }

  public abstract void transform(ClassNode node);

  @Nonnull
  @Override
  public TransformerVoteResult castVote(ITransformerVotingContext context) {
    return TransformerVoteResult.YES;
  }

  @Nonnull
  @Override
  public Set<Target> targets() {
    return Collections.singleton(Target.targetClass(getTransformingClassName()));
  }
}
