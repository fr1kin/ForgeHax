package com.matt.forgehax.mods;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FullBrightMod extends ToggleMod {
    public FullBrightMod() {
        super("FullBright", false, "Makes everything render with maximum brightness");
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
