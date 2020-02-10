package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.Set;

public interface ITransformerProvider<E> extends ITransformer<E> {
  default ITransformer<ClassNode> toClassNodeTransformer() {
    ITransformer<ClassNode> provider = (ITransformer<ClassNode>) this;
    return new ITransformer<ClassNode>() {
      @Nonnull
      @Override
      public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        return provider.transform(input, context);
      }

      @Nonnull
      @Override
      public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return provider.castVote(context);
      }

      @Nonnull
      @Override
      public Set<Target> targets() {
        return provider.targets();
      }

      @Override
      public String[] labels() {
        return provider.labels();
      }
    };
  }

  default ITransformer<MethodNode> toMethodNodeTransformer() {
    ITransformer<MethodNode> provider = (ITransformer<MethodNode>) this;
    return new ITransformer<MethodNode>() {
      @Nonnull
      @Override
      public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        return provider.transform(input, context);
      }

      @Nonnull
      @Override
      public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return provider.castVote(context);
      }

      @Nonnull
      @Override
      public Set<Target> targets() {
        return provider.targets();
      }

      @Override
      public String[] labels() {
        return provider.labels();
      }
    };
  }
}
