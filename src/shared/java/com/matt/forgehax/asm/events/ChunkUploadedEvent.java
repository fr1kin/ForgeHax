package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/7/2017 by fr1kin */
public class ChunkUploadedEvent extends Event {
  private final ChunkRender renderChunk;
  private final BufferBuilder buffer;

  public ChunkUploadedEvent(ChunkRender renderChunk, BufferBuilder BufferBuilder) {
    this.renderChunk = renderChunk;
    this.buffer = BufferBuilder;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }

  public BufferBuilder getBuffer() {
    return buffer;
  }
}
