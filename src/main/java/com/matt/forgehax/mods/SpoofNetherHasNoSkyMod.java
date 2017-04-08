package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;

/**
 * Created on 4/1/2017 by fr1kin
 */
public class SpoofNetherHasNoSkyMod extends ToggleMod {
    public SpoofNetherHasNoSkyMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.spoofedNetherHasNoSky = false;
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.spoofedNetherHasNoSky = true;
    }
}
