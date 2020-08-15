package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import lombok.AllArgsConstructor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.Set;

public class Wrappers {
  @SuppressWarnings("unchecked")
  public static <T> ITransformer<T> createWrapper(ITransformer<T> transformer) {
    if (transformer instanceof PatchScanner.InternalMethodTransformer) {
      return (ITransformer<T>) new ClassTransformerWrapper((ITransformer<ClassNode>) transformer);
    } /*else if (transformer instanceof ClassTransformer) {
      return (ITransformer<T>) new ClassTransformerWrapper((ITransformer<ClassNode>) transformer);
    }*/ else {
      throw new IllegalArgumentException("Class \"" + transformer.getClass().getSimpleName() + " is not supported.");
    }

  }

  @AllArgsConstructor
  private static class ClassTransformerWrapper implements ITransformer<ClassNode> {
    private final ITransformer<ClassNode> impl;

    @Nonnull
    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
      return impl.transform(input, context);
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
      return impl.castVote(context);
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
      return impl.targets();
    }
  }

  @AllArgsConstructor
  private static class FieldTransformerWrapper implements ITransformer<FieldNode> {
    private final ITransformer<FieldNode> impl;

    @Nonnull
    @Override
    public FieldNode transform(FieldNode input, ITransformerVotingContext context) {
      return impl.transform(input, context);
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
      return impl.castVote(context);
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
      return impl.targets();
    }
  }

  @AllArgsConstructor
  private static class MethodTransformerWrapper implements ITransformer<MethodNode> {
    private final ITransformer<MethodNode> impl;

    @Nonnull
    @Override
    public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
      return impl.transform(input, context);
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
      return impl.castVote(context);
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
      return impl.targets();
    }
  }

}
