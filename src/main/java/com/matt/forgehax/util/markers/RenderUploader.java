package com.matt.forgehax.util.markers;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.reflection.FastReflection;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 1/18/2018 by fr1kin
 */
public class RenderUploader<E extends Tessellator> implements Globals {
    private final Uploaders<E> parent;
    private final ReentrantLock _lock = new ReentrantLock();

    /**
     * The vertex buffer instance (for uploading the tessellator data)
     */
    private final VertexBuffer vertexBuffer;

    /**
     * The tessellator instance
     */
    private volatile E tessellator;

    /**
     * The thread the current VBO is being processed on
     */
    private volatile Thread currentThread;

    private boolean drawing = false;
    private boolean uploaded = false;
    private int renderCount = 0;
    private BlockPos region = null;

    public RenderUploader(Uploaders<E> parent, VertexFormat format) {
        this.parent = parent;
        vertexBuffer = new VertexBuffer(format);
    }
    public RenderUploader(Uploaders<E> parent) {
        this(parent, DefaultVertexFormats.POSITION_COLOR);
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }

    /**
     * Gets the tessellator
     * @return volatile tessellator
     */
    public E getTessellator() {
        return tessellator;
    }
    public void setTessellator(E tessellator) throws UploaderException {
        if(tessellator != null && this.tessellator != null)
            throw new UploaderException("Tried to set tessellator without removing the previous one");
        else
            this.tessellator = tessellator;
    }
    public void takeTessellator() throws UploaderException, InterruptedException {
        setTessellator(parent.cache().take());
    }
    public void freeTessellator() throws UploaderException, TessellatorCache.TessellatorCacheFreeException {
        if(tessellator != null) {
            // stop tessellator from drawing
            if(isTessellatorDrawing()) getBufferBuilder().finishDrawing();
            // reset translation
            getBufferBuilder().setTranslation(0.D, 0.D, 0.D);
            // free tessellator to parent
            parent.cache().free(tessellator);
            // set tessellator to null
            setTessellator(null);
        }
    }

    public BufferBuilder getBufferBuilder() {
        return getTessellator().getBuffer();
    }

    /**
     * Will set the current thread to the current thread running the method.
     */
    public void setCurrentThread() {
        currentThread = Thread.currentThread();
    }
    public void nullifyCurrentThread() {
        currentThread = null;
    }
    /**
     * Verify that the current thread is the one doing the rendering
     * @throws ThreadMismatchException if running in the incorrect thread
     */
    public void validateCurrentThread() throws ThreadMismatchException {
        if(currentThread != Thread.currentThread())
            throw new ThreadMismatchException("Tried executing in incorrect thread (this is normal)");
    }
    /**
     * Same thing but returns a boolean instead of throwing error
     * @return
     */
    public boolean isCorrectCurrentThread() {
        return currentThread == Thread.currentThread();
    }

    /**
     * Get the vertex buffer used to upload
     * @return
     */
    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    /**
     * Upload the vertex buffer to the GPU
     * @throws UploaderException if the upload failed
     */
    public void upload() throws UploaderException {
        if(getTessellator() == null) return; // no tessellator
        if(!MC.isCallingFromMinecraftThread()) throw new UploaderException("Not calling from main Minecraft thread");
        if(isTessellatorDrawing()) throw new UploaderException("Tried to upload VBO while tessellator is still drawing");

        try {
            getBufferBuilder().reset();
            vertexBuffer.bufferData(getBufferBuilder().getByteBuffer());
        } finally {
            setDrawing(true);
            uploaded = true;
        }
    }

    /**
     * Unload the vertex buffer from the GPU
     * @throws UploaderException if the unload failed
     */
    public void unload() throws UploaderException {
        if(!isUploaded()) return;
        if(!MC.isCallingFromMinecraftThread()) throw new UploaderException("Not calling from main Minecraft thread");

        try {
            vertexBuffer.deleteGlBuffers();
        } finally {
            uploaded = false;
            setDrawing(false);
            resetRegion();
        }
    }

    /**
     * Check if the tessellator instance is current drawing
     * @return
     */
    public boolean isTessellatorDrawing() {
        return getTessellator() != null && FastReflection.Fields.BufferBuilder_isDrawing.get(getBufferBuilder());
    }

    public void finishDrawing() {
        if(!isTessellatorDrawing()) return;

        getBufferBuilder().finishDrawing();
        renderCount = getBufferBuilder().getVertexCount() / 24;
    }

    /**
     * Check if the vertex buffer has been uploaded
     * @return
     */
    public boolean isUploaded() {
        return uploaded;
    }

    /**
     * Number of objects currently added to the tessellator
     * @return number
     */
    public int getRenderCount() {
        return renderCount;
    }
    public void resetRenderCount() {
        renderCount = 0;
    }

    public void setRegion(RenderChunk chunk) {
        region = new BlockPos(chunk.getPosition()); // copy because RenderChunk.position is mutable
    }
    public void resetRegion() {
        region = null;
    }
    public BlockPos getRegion() {
        return region;
    }
    public boolean isCorrectRegion(RenderChunk chunk) {
        return region != null && region.equals(chunk.getPosition());
    }

    public ReentrantLock lock() {
        return _lock;
    }

    public static class UploaderException extends Exception {
        public UploaderException(String msg) {
            super(msg);
        }
    }
    public static class ThreadMismatchException extends UploaderException {
        public ThreadMismatchException(String msg) {
            super(msg);
        }
    }
    public static class VertexBufferException extends UploaderException {
        public VertexBufferException(String msg) {
            super(msg);
        }
    }
}
