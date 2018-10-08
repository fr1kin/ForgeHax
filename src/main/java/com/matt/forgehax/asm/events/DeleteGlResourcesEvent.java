package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.chunk.RenderChunk;
import com.matt.forgehax.util.event.Event;

/**
 * Created on 5/8/2017 by fr1kin
 */
public class DeleteGlResourcesEvent extends Event {
    private final RenderChunk renderChunk;

    public DeleteGlResourcesEvent(RenderChunk renderChunk) {
        this.renderChunk = renderChunk;
    }

    public RenderChunk getRenderChunk() {
        return renderChunk;
    }
}
