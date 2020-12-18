package dev.fiki.forgehax.asm.events.packet;

import dev.fiki.forgehax.api.event.Cancelable;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;

@Cancelable
public class PacketOutboundEvent extends PacketEvent {
  public PacketOutboundEvent(NetworkManager manager, IPacket<?> packet) {
    super(manager, packet);
  }
}
