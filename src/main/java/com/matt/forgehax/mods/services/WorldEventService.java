package com.matt.forgehax.mods.services;

import com.matt.forgehax.events.ClientWorldEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class WorldEventService extends ServiceMod {
  public WorldEventService() {
    super("WorldEventService");
  }
  
  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if(event.getWorld() instanceof ClientWorld) {
      MinecraftForge.EVENT_BUS.post(new ClientWorldEvent.Load((ClientWorld) event.getWorld()));
    }
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    if(event.getWorld() instanceof ClientWorld) {
      MinecraftForge.EVENT_BUS.post(new ClientWorldEvent.Unload((ClientWorld) event.getWorld()));
    }
  }
}
