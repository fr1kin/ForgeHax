package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.TransformTargetLabel;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Wrappers {

    // TODO: only wrap if necessary
    @SuppressWarnings("unchecked")
    public static <T> ITransformer<T> createWrapper(ITransformer<T> transformer, RegisterTransformer annotation) {
        Optional<ParameterizedType> parameterizedType =  Stream.of(transformer.getClass().getGenericInterfaces())
                .filter(type -> type instanceof ParameterizedType)
                .map(type -> (ParameterizedType)type)
                .filter(pType -> pType.getRawType().equals(Wrappers.class) || pType.getRawType().equals(ITransformer.class))
                .findFirst();

        Type nodeType = parameterizedType
                .map(pType -> pType.getActualTypeArguments()[0])
                .orElseGet(annotation::nodeType);

        TransformTargetLabel.LabelType labelType = TransformTargetLabel.LabelType.getTypeFor(nodeType)
            .orElseThrow(() -> new IllegalStateException("Class " + transformer.getClass() + " attempted to implement transformer for invalid node type"));
        switch (labelType) {
            case FIELD: return  (ITransformer<T>)  new FieldTransformerWrapper((ITransformer<FieldNode>)transformer);
            case METHOD: return (ITransformer<T>) new MethodTransformerWrapper((ITransformer<MethodNode>)transformer);
            case CLASS: return  (ITransformer<T>)  new ClassTransformerWrapper((ITransformer<ClassNode>)transformer);

            default: throw new IllegalStateException("??? " + transformer.getClass());
        }

    }

  private static class ClassTransformerWrapper implements ITransformer<ClassNode> {
    private final ITransformer<ClassNode> impl;

    public ClassTransformerWrapper(ITransformer<ClassNode> impl) {
      this.impl = impl;
    }

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

  private static class FieldTransformerWrapper implements ITransformer<FieldNode> {
    private final ITransformer<FieldNode> impl;

    public FieldTransformerWrapper(ITransformer<FieldNode> impl) {
      this.impl = impl;
    }

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

  private static class MethodTransformerWrapper implements ITransformer<MethodNode> {
    private final ITransformer<MethodNode> impl;

    public MethodTransformerWrapper(ITransformer<MethodNode> impl) {
      this.impl = impl;
    }

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
