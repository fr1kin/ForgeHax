package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.events.PostClientTickEvent;
import dev.fiki.forgehax.api.events.PreClientTickEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ClientTickEventService extends ServiceMod {
  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if(TickEvent.Phase.START.equals(event.phase)) {
      MinecraftForge.EVENT_BUS.post(new PreClientTickEvent());
    } else {
      MinecraftForge.EVENT_BUS.post(new PostClientTickEvent());
    }
  }
}
