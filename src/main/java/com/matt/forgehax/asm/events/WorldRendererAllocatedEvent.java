package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/11/2017 by fr1kin */
public class WorldRendererAllocatedEvent extends Event {
  private final ChunkRenderTask generator;
  private final ChunkRender renderChunk;

  public WorldRendererAllocatedEvent(ChunkRenderTask generator, ChunkRender renderChunk) {
    this.generator = generator;
    this.renderChunk = renderChunk;
  }

  public ChunkRenderTask getGenerator() {
    return generator;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }
}
