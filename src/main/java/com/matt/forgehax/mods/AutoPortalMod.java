package com.matt.forgehax.mods;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 1/2/2017 by fr1kin
 */
public class AutoPortalMod extends ToggleMod {
    public Property waitTime;

    public AutoPortalMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {

    }
}
