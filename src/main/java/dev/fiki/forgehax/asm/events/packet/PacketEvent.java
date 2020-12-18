package dev.fiki.forgehax.asm.events.packet;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class PacketEvent extends Event {
  private static final LoadingCache<IPacket<?>, Boolean> CACHE = CacheBuilder.newBuilder()
      .expireAfterWrite(15L, TimeUnit.SECONDS)
      .build(new CacheLoader<IPacket<?>, Boolean>() {
        @Override
        public Boolean load(IPacket<?> key) throws Exception {
          return false;
        }
      });

  public static void ignore(IPacket<?> packet) {
    CACHE.put(packet, true);
  }

  public static void remove(IPacket<?> packet) {
    CACHE.invalidate(packet);
  }

  public static boolean isIgnored(IPacket<?> packet) {
    return CACHE.getUnchecked(packet) != null;
  }

  private final NetworkManager manager;
  private final IPacket<?> packet;

  public final boolean isIgnored() {
    return isIgnored(packet);
  }
}
