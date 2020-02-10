package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.fiki.forgehax.asm.TypesMc;
import dev.fiki.forgehax.asm.utils.ASMHelper;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public abstract class MethodTransformer implements ITransformer<MethodNode>, TypesMc, Opcodes, ASMHelper.MagicOpcodes {

  
  public abstract ASMMethod getMethod();
  public abstract void transform(MethodNode method);


  @Nonnull
  @Override
  public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
    // TODO: if this is null use the class name for the desc or something
    final @Nullable RegisterTransformer annotation = this.getClass().getAnnotation(RegisterTransformer.class);

    getLogger().debug("Transforming method {}::{}[{}]",
        getMethod().getParent().getClassName(),
        getMethod().getMcp(), getMethod().getMcpDescriptor());

      try {
        this.transform(input);
        getLogger().debug("Successfully transformed task \"{}\"", annotation.value());
      } catch (Throwable t) {
        if(t instanceof InvocationTargetException) {
          // we don't care about the reflection error
          t = t.getCause();
        }
        // catch errors
        getLogger().error("Failed to transform task \"{}\" in method {}::{}[{}]",
            annotation.value(),
            getMethod().getParent().getClassName(),
            getMethod().getMcp(), getMethod().getMcpDescriptor());
        getLogger().error(t, t);
      }

    return input;
  }

  @Nonnull
  @Override
  public TransformerVoteResult castVote(ITransformerVotingContext context) {
    return TransformerVoteResult.YES;
  }

  @Nonnull
  @Override
  public Set<Target> targets() {
    return Stream.of(getMethod().toSrgTransformerTarget(), getMethod().toMcpTransformerTarget())
        .map(DistinctTarget::new)
        .distinct()
        .map(DistinctTarget::getTarget)
        .collect(Collectors.toSet());
  }


  @Getter
  @AllArgsConstructor
  static class DistinctTarget {
    private final ITransformer.Target target;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DistinctTarget that = (DistinctTarget) o;
      return Objects.equals(target.getElementName(), that.target.getElementName())
          && Objects.equals(target.getElementDescriptor(), that.target.getElementDescriptor());
    }

    @Override
    public int hashCode() {
      return Objects.hash(target.getElementName(), target.getElementDescriptor());
    }

    @Override
    public String toString() {
      return target.getClassName() + "::" +  target.getElementName() + target.getElementDescriptor();
    }
  }
}
