package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class LocalPlayerUpdateEventService extends ServiceMod {
  public LocalPlayerUpdateEventService() {
    super("LocalPlayerUpdateEventService");
  }

  @SubscribeEvent
  public void onUpdate(LivingEvent.LivingUpdateEvent event) {
    if (getWorld() != null
        && !event.getEntity().getEntityWorld().isRemote
        && event.getEntityLiving().equals(getLocalPlayer())) {
      Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
      MinecraftForge.EVENT_BUS.post(ev);
      event.setCanceled(ev.isCanceled());
    }
  }
}
