package com.matt.forgehax.asm.transformer;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.Set;

public class MethodTransformerWrapper implements ITransformer<MethodNode> {
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
