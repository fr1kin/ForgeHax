package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.asm.events.replacementhooks.ClientTickEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class FullBrightMod extends ToggleMod {
    public FullBrightMod() {
        super(Category.WORLD, "FullBright", false, "Makes everything render with maximum brightness");
    }

    @Override
    public void onEnabled() {
        MC.gameSettings.gammaSetting = 16F;
    }

    @Override
    public void onDisabled() {
        MC.gameSettings.gammaSetting = 1F;
    }

    @Subscribe
    public void onClientTick(ClientTickEvent event) {
        MC.gameSettings.gammaSetting = 16F;
    }
}
