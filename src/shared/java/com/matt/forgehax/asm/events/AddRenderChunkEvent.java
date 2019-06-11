package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/9/2017 by fr1kin */
public class AddRenderChunkEvent extends Event {
  private final ChunkRender renderChunk;
  private final BlockRenderLayer blockRenderLayer;

  public AddRenderChunkEvent(ChunkRender renderChunk, BlockRenderLayer blockRenderLayer) {
    this.renderChunk = renderChunk;
    this.blockRenderLayer = blockRenderLayer;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }

  public BlockRenderLayer getBlockRenderLayer() {
    return blockRenderLayer;
  }
}
