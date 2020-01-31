package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class LocalPlayerUpdateEventService extends ServiceMod {
  
  public LocalPlayerUpdateEventService() {
    super("LocalPlayerUpdateEventService");
  }
  
  @SubscribeEvent
  public void onUpdate(LivingEvent.LivingUpdateEvent event) {
    if (Globals.getWorld() != null
        && event.getEntity().getEntityWorld().isRemote
        && event.getEntityLiving().equals(Globals.getLocalPlayer())) {
      Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
      MinecraftForge.EVENT_BUS.post(ev);
      event.setCanceled(ev.isCanceled());
    }
  }
}
