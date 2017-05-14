package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/7/2017 by fr1kin
 */
public class ChunkUploadedEvent extends Event {
    private final RenderChunk renderChunk;
    private final VertexBuffer buffer;

    public ChunkUploadedEvent(RenderChunk renderChunk, VertexBuffer vertexBuffer) {
        this.renderChunk = renderChunk;
        this.buffer = vertexBuffer;
    }

    public RenderChunk getRenderChunk() {
        return renderChunk;
    }

    public VertexBuffer getBuffer() {
        return buffer;
    }
}
