package dev.fiki.forgehax.asm.events.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class PacketOutboundEvent extends Event {
  private final NetworkManager networkManager;
  private final IPacket<?> packet;
}
