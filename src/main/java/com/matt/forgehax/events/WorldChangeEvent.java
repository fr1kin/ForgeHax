package com.matt.forgehax.events;

import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.WorldEvent;

/** Created on 5/29/2017 by fr1kin */
public class WorldChangeEvent extends WorldEvent {
  public WorldChangeEvent(IWorld world) {
    super(world);
  }

  public boolean isWorldNull() {
    return getWorld() == null;
  }
}
