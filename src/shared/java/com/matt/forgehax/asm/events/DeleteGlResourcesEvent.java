package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/8/2017 by fr1kin */
public class DeleteGlResourcesEvent extends Event {
  private final ChunkRender renderChunk;

  public DeleteGlResourcesEvent(ChunkRender renderChunk) {
    this.renderChunk = renderChunk;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }
}
