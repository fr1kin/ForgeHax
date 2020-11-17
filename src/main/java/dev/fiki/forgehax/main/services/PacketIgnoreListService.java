package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.PacketHelper;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class PacketIgnoreListService extends ServiceMod {
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onSentPacket(PacketOutboundEvent event) {
    if (PacketHelper.isIgnored(event.getPacket())) {
      PacketHelper.remove(event.getPacket());
    }
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onIncomingPacket(PacketInboundEvent event) {
    if (PacketHelper.isIgnored(event.getPacket())) {
      PacketHelper.remove(event.getPacket());
    }
  }
}
