package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiFireMod extends ToggleMod {
    public AntiFireMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        event.getEntityLiving().extinguish();
    }
}
