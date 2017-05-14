package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.ComputeVisibilityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoCaveCulling extends ToggleMod {
    public NoCaveCulling() {
        super("NoCaveCulling", false, "Disables mojangs dumb cave culling shit");
    }

    @Override
    public void onEnabled() {
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable();
    }

    @Override
    public void onDisabled() {
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable();
    }
}
