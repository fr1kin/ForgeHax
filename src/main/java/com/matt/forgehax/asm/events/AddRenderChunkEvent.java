package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/9/2017 by fr1kin
 */
public class AddRenderChunkEvent extends Event {
  
  private final RenderChunk renderChunk;
  private final BlockRenderLayer blockRenderLayer;
  
  public AddRenderChunkEvent(RenderChunk renderChunk, BlockRenderLayer blockRenderLayer) {
    this.renderChunk = renderChunk;
    this.blockRenderLayer = blockRenderLayer;
  }
  
  public RenderChunk getRenderChunk() {
    return renderChunk;
  }
  
  public BlockRenderLayer getBlockRenderLayer() {
    return blockRenderLayer;
  }
}
