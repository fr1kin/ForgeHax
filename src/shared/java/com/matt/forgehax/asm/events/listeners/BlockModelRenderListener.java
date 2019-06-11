package com.matt.forgehax.asm.events.listeners;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.math.BlockPos;

/** Created on 5/8/2017 by fr1kin */
public interface BlockModelRenderListener extends ListenerHook {
  void onBlockRenderInLoop(ChunkRender renderChunk, Block block, BlockState state, BlockPos pos);
}
