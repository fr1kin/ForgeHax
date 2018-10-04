package com.matt.forgehax.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.Packet;

/** Created on 8/4/2017 by fr1kin */
public class PacketHelper {
  private static final LoadingCache<Packet, Boolean> CACHE =
      CacheBuilder.newBuilder()
          .expireAfterWrite(15L, TimeUnit.SECONDS)
          .build(
              new CacheLoader<Packet, Boolean>() {
                @Override
                public Boolean load(Packet key) throws Exception {
                  return false;
                }
              });

  public static void ignore(Packet packet) {
    CACHE.put(packet, true);
  }

  public static boolean isIgnored(Packet packet) {
    try {
      return CACHE.get(packet);
    } catch (ExecutionException e) {
      return false;
    }
  }

  public static void remove(Packet packet) {
    CACHE.invalidate(packet);
  }
}
