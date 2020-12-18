package dev.fiki.forgehax.api.events.world;

import net.minecraft.client.world.ClientWorld;

public class WorldUnloadEvent extends WorldChangeEvent {
  public WorldUnloadEvent(ClientWorld world) {
    super(world);
  }
}
