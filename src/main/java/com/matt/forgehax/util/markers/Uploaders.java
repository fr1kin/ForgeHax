package com.matt.forgehax.util.markers;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;

/** Created on 1/18/2018 by fr1kin */
public class Uploaders<E extends Tessellator> {
  private final Map<RenderChunk, RenderUploader<E>> uploaders = Maps.newConcurrentMap();
  private final UploaderSupplier<E> supplier;

  private final TessellatorCache<E> cache;

  private Consumer<RenderUploader<E>> shutdownTask;

  public Uploaders(UploaderSupplier<E> supplier, TessellatorCache<E> cache) {
    this.supplier = supplier;
    this.cache = cache;
  }

  /**
   * Register RenderChunk and create new RenderUploader instance for it
   *
   * @param renderChunk
   */
  public void register(RenderChunk renderChunk) {
    RenderUploader<E> uploader = uploaders.get(renderChunk);
    // if a key for this object already exists, notify the shutdown hook and remove the old entry
    if (uploader != null && shutdownTask != null) shutdownTask.accept(uploader);
    uploaders.put(renderChunk, supplier.get(this));
  }

  /**
   * Unregister RenderChunk
   *
   * @param renderChunk
   */
  public void unregister(RenderChunk renderChunk) {
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

  public Optional<RenderUploader<E>> get(RenderChunk renderChunk) {
    return Optional.ofNullable(uploaders.get(renderChunk));
  }

  /**
   * Current size of the RenderChunk map
   *
   * @return
   */
  public int size() {
    return uploaders.size();
  }

  public void computeIfPresent(RenderChunk renderChunk, final Consumer<RenderUploader<E>> task) {
    RenderUploader<E> uploader = uploaders.get(renderChunk);
    if (uploader != null) task.accept(uploader);
  }

  public void forEach(BiConsumer<RenderChunk, RenderUploader<E>> action) {
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

  public static boolean isDummy(RenderChunk chunk) {
    return chunk != null && chunk.getCompiledChunk() == CompiledChunk.DUMMY;
  }

  public interface UploaderSupplier<T extends Tessellator> {
    RenderUploader<T> get(Uploaders<T> parent);
  }
}
