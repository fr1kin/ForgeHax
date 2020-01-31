package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class PacketIgnoreListService extends ServiceMod {
  
  public PacketIgnoreListService() {
    super("PacketIgnoreListService");
  }
  
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
