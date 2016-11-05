package com.matt.forgehax.mods.debug;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.mods.ToggleMod;

public class DebugModeMod extends ToggleMod {
    public DebugModeMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.isInDebugMode = true;
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.isInDebugMode = false;
    }
}
