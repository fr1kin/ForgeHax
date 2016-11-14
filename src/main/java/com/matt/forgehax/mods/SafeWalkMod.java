package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;

/**
 * Created on 9/4/2016 by fr1kin
 */
public class SafeWalkMod extends ToggleMod {
    public SafeWalkMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.isSafeWalkActivated = true;
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.isSafeWalkActivated = false;
    }
}
