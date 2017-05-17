package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.mod.loader.RegisterMod;

/**
 * Created on 9/4/2016 by fr1kin
 */

@RegisterMod
public class SafeWalkMod extends ToggleMod {
    public SafeWalkMod() {
        super("SafeWalk", false, "Prevents you from falling off blocks");
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
