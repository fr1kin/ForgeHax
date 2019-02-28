package com.matt.forgehax.asm.transformer;

import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.utils.ASMHelper;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;

public interface Transformer<T> extends ITransformer<T>, ASMCommon, TypesMc, Opcodes, ASMHelper.MagicOpcodes {

    @Nonnull
    @Override
    default TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

}
