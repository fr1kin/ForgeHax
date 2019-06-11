package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraftforge.eventbus.api.Event;

/** Created on 5/8/2017 by fr1kin */
public class BuildChunkEvent extends Event {
  private final ChunkRender renderChunk;

  public BuildChunkEvent(ChunkRender renderChunk) {
    this.renderChunk = renderChunk;
  }

  public ChunkRender getRenderChunk() {
    return renderChunk;
  }

  public static class Pre extends BuildChunkEvent {
    public Pre(ChunkRender renderChunk) {
      super(renderChunk);
    }
  }

  public static class Post extends BuildChunkEvent {
    public Post(ChunkRender renderChunk) {
      super(renderChunk);
    }
  }
}
