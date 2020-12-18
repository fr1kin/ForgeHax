package dev.fiki.forgehax.api.events.world;

import net.minecraft.client.world.ClientWorld;

public class WorldLoadEvent extends WorldChangeEvent {
  public WorldLoadEvent(ClientWorld world) {
    super(world);
  }
}
