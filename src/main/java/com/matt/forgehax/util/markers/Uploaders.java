package com.matt.forgehax.util.markers;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRender;

/** Created on 1/18/2018 by fr1kin */
public class Uploaders<E extends Tessellator> {
  private final Map<ChunkRender, RenderUploader<E>> uploaders = Maps.newConcurrentMap();
  private final UploaderSupplier<E> supplier;

  private final TessellatorCache<E> cache;

  private Consumer<RenderUploader<E>> shutdownTask;

  public Uploaders(UploaderSupplier<E> supplier, TessellatorCache<E> cache) {
    this.supplier = supplier;
    this.cache = cache;
  }

  /**
   * Register ChunkRender and create new RenderUploader instance for it
   *
   * @param renderChunk
   */
  public void register(ChunkRender renderChunk) {
    RenderUploader<E> uploader = uploaders.get(renderChunk);
    // if a key for this object already exists, notify the shutdown hook and remove the old entry
    if (uploader != null && shutdownTask != null) shutdownTask.accept(uploader);
    uploaders.put(renderChunk, supplier.get(this));
  }

  /**
   * Unregister ChunkRender
   *
   * @param renderChunk
   */
  public void unregister(ChunkRender renderChunk) {
    RenderUploader<E> uploader = uploaders.get(renderChunk);
    // if a key for this object already exists, notify the shutdown hook and remove the old entry
    if (uploader != null && shutdownTask != null) {
      shutdownTask.accept(uploader);
      uploaders.remove(renderChunk);
    }
  }

  /** Unregister all added RenderChunks */
  public void unregisterAll() {
    forEach((k, v) -> unregister(k));
  }

  public Optional<RenderUploader<E>> get(ChunkRender renderChunk) {
    return Optional.ofNullable(uploaders.get(renderChunk));
  }

  /**
   * Current size of the ChunkRender map
   *
   * @return
   */
  public int size() {
    return uploaders.size();
  }

  public void computeIfPresent(ChunkRender renderChunk, final Consumer<RenderUploader<E>> task) {
    RenderUploader<E> uploader = uploaders.get(renderChunk);
    if (uploader != null) task.accept(uploader);
  }

  public void forEach(BiConsumer<ChunkRender, RenderUploader<E>> action) {
    uploaders.forEach(action);
  }

  /**
   * Set the shutdown hook for when a RenderUploader is no longer needed
   *
   * @param shutdownTask task
   */
  public void onShutdown(Consumer<RenderUploader<E>> shutdownTask) {
    this.shutdownTask = shutdownTask;
  }

  public TessellatorCache<E> cache() {
    return cache;
  }

  public static boolean isDummy(ChunkRender chunk) {
    return chunk != null && chunk.getCompiledChunk() == CompiledChunk.DUMMY;
  }

  public interface UploaderSupplier<T extends Tessellator> {
    RenderUploader<T> get(Uploaders<T> parent);
  }
}
