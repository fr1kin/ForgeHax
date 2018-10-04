package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Created on 5/10/2017 by fr1kin */
public class LoadRenderersEvent extends Event {
  private final ViewFrustum viewFrustum;
  private final ChunkRenderDispatcher renderDispatcher;

  public LoadRenderersEvent(ViewFrustum viewFrustum, ChunkRenderDispatcher renderDispatcher) {
    this.viewFrustum = viewFrustum;
    this.renderDispatcher = renderDispatcher;
  }

  public ViewFrustum getViewFrustum() {
    return viewFrustum;
  }

  public ChunkRenderDispatcher getRenderDispatcher() {
    return renderDispatcher;
  }
}
