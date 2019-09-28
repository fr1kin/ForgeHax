package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class PacketIgnoreListService extends ServiceMod {
  
  public PacketIgnoreListService() {
    super("PacketIgnoreListService");
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onSentPacket(PacketEvent.Outgoing.Post event) {
    if (PacketHelper.isIgnored(event.getPacket())) {
      PacketHelper.remove(event.getPacket());
    }
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onIncomingPacket(PacketEvent.Incoming.Post event) {
    if (PacketHelper.isIgnored(event.getPacket())) {
      PacketHelper.remove(event.getPacket());
    }
  }
}
