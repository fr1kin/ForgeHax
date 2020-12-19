package dev.fiki.forgehax.asm.events.packet;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;

import java.util.WeakHashMap;

@Getter
@AllArgsConstructor
public class PacketEvent extends Event {
  private static final WeakHashMap<IPacket<?>, Boolean> CACHE = new WeakHashMap<>();

  public static void ignore(IPacket<?> packet) {
    CACHE.put(packet, Boolean.TRUE);
  }

  public static void remove(IPacket<?> packet) {
    CACHE.remove(packet);
  }

  public static boolean isIgnored(IPacket<?> packet) {
    return CACHE.getOrDefault(packet, Boolean.FALSE);
  }

  private final NetworkManager manager;
  private final IPacket<?> packet;

  public final boolean isIgnored() {
    return isIgnored(packet);
  }
}
