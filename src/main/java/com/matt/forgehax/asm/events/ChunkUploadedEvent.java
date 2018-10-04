package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Created on 5/7/2017 by fr1kin */
public class ChunkUploadedEvent extends Event {
  private final RenderChunk renderChunk;
  private final BufferBuilder buffer;

  public ChunkUploadedEvent(RenderChunk renderChunk, BufferBuilder BufferBuilder) {
    this.renderChunk = renderChunk;
    this.buffer = BufferBuilder;
  }

  public RenderChunk getRenderChunk() {
    return renderChunk;
  }

  public BufferBuilder getBuffer() {
    return buffer;
  }
}
