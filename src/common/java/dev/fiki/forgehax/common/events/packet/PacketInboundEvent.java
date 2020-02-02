package dev.fiki.forgehax.common.events.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class PacketInboundEvent extends Event {
  private final NetworkManager manager;
  private final IPacket<?> packet;
}
