package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/8/2017 by fr1kin
 */
public class BuildChunkEvent extends Event {
  
  private final RenderChunk renderChunk;
  
  public BuildChunkEvent(RenderChunk renderChunk) {
    this.renderChunk = renderChunk;
  }
  
  public RenderChunk getRenderChunk() {
    return renderChunk;
  }
  
  public static class Pre extends BuildChunkEvent {
    
    public Pre(RenderChunk renderChunk) {
      super(renderChunk);
    }
  }
  
  public static class Post extends BuildChunkEvent {
    
    public Post(RenderChunk renderChunk) {
      super(renderChunk);
    }
  }
}
