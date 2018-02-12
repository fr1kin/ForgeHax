package com.matt.forgehax.asm.events.listeners;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface BlockModelRenderListener extends ListenerHook {
    void onBlockRenderInLoop(RenderChunk renderChunk, Block block, IBlockState state, BlockPos pos);
}
