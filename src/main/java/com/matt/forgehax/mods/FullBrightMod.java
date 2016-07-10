package com.matt.forgehax.mods;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FullBrightMod extends ToggleMod {
    public FullBrightMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        MC.gameSettings.gammaSetting = 16F;
    }

    @Override
    public void onDisabled() {
        MC.gameSettings.gammaSetting = 1F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        MC.gameSettings.gammaSetting = 16F;
    }
}
