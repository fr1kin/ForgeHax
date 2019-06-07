package com.matt.forgehax.asm.coremod.transformer;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.Nonnull;
import java.util.Set;

public class FieldTransformerWrapper implements ITransformer<FieldNode> {
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
