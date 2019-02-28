package com.matt.forgehax.asm.transformer;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.util.Set;

public class ClassTransformerWrapper implements ITransformer<ClassNode> {
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
