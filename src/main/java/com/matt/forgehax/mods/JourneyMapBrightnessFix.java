package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.List;

/**
 * Created on 5/4/2017 by fr1kin
 */
public class JourneyMapBrightnessFix extends ToggleMod {
    public JourneyMapBrightnessFix() {
        super("JourneyMapLightingFix", false, "Fixes journey map lighting bug");
        setHidden(true);
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
