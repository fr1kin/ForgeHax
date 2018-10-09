package com.matt.forgehax.events;

import com.matt.forgehax.asm.events.replacementhooks.WorldEvent;
import net.minecraft.world.World;

/**
 * Created on 5/29/2017 by fr1kin
 */
public class WorldChangeEvent extends WorldEvent {
    public WorldChangeEvent(World world) {
        super(world);
    }

    public boolean isWorldNull() {
        return getWorld() == null;
    }
}
