package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/11/2017 by fr1kin */
public class WorldRendererDeallocatedEvent extends Event {
  private final ChunkCompileTaskGenerator generator;
  private final ChunkRender renderChunk;

  public WorldRendererDeallocatedEvent(
      ChunkCompileTaskGenerator generator, ChunkRender renderChunk) {
    this.generator = generator;
    this.renderChunk = renderChunk;
  }

  public ChunkCompileTaskGenerator getGenerator() {
    return generator;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }
}
