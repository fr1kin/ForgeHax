package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/11/2017 by fr1kin
 */
public class WorldRendererAllocatedEvent extends Event {
  
  private final ChunkCompileTaskGenerator generator;
  private final RenderChunk renderChunk;
  
  public WorldRendererAllocatedEvent(ChunkCompileTaskGenerator generator, RenderChunk renderChunk) {
    this.generator = generator;
    this.renderChunk = renderChunk;
  }
  
  public ChunkCompileTaskGenerator getGenerator() {
    return generator;
  }
  
  public RenderChunk getRenderChunk() {
    return renderChunk;
  }
}
