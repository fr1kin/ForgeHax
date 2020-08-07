package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.events.ClientWorldEvent;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class WorldEventService extends ServiceMod {
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
