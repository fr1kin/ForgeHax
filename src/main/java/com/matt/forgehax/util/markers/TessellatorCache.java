package com.matt.forgehax.util.markers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.matt.forgehax.Globals;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import net.minecraft.client.renderer.Tessellator;

/** Created on 1/18/2018 by fr1kin */
public class TessellatorCache<E extends Tessellator> implements Globals {
  private final BlockingQueue<E> cache;
  private final List<E> originals;

  public TessellatorCache(int capacity, Supplier<E> supplier) {
    cache = Queues.newArrayBlockingQueue(capacity);

    // fill the cache
    for (int i = 0; i < capacity; i++) cache.add(supplier.get());

    // copy list of the original tessellators to prevent others from being added
    originals = ImmutableList.copyOf(cache);
  }

  public E take() throws InterruptedException {
    return cache.take();
  }

  public boolean free(E tessellator) throws TessellatorCacheFreeException {
    if (!originals.contains(tessellator))
      throw new TessellatorCacheFreeException(
          "Tried to add tessellator that wasn't originally in cache");
    return tessellator != null && cache.offer(tessellator);
  }

  public int size() {
    return cache.size();
  }

  public int capacity() {
    return originals.size();
  }

  public static class TessellatorCacheFreeException extends Exception {
    public TessellatorCacheFreeException(String msg) {
      super(msg);
    }
  }
}
