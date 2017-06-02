package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

/**
 * Created on 5/4/2017 by fr1kin
 */

@RegisterMod
public class JourneyMapBrightnessFix extends ToggleMod {
    public JourneyMapBrightnessFix() {
        super("JourneyMapLightingFix", false, "Fixes journey map lighting bug");
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.ENABLE_JOURNEYMAP_LIGHTING_FIX = true;
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.ENABLE_JOURNEYMAP_LIGHTING_FIX = false;
    }
}
