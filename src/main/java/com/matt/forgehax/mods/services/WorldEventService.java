package com.matt.forgehax.mods.services;

import com.matt.forgehax.events.WorldChangeEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class WorldEventService extends ServiceMod {

  public WorldEventService() {
    super("WorldEventService");
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
  }
}
