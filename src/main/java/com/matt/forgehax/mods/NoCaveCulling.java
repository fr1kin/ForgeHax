package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.ComputeVisibilityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoCaveCulling extends ToggleMod {
    public NoCaveCulling(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    public void reloadRenderers() {
        if(MC.renderGlobal != null) {
            MC.renderGlobal.loadRenderers();
        }
    }

    @Override
    public void onEnabled() {
        reloadRenderers();
    }

    @Override
    public void onDisabled() {
        reloadRenderers();
    }

    @SubscribeEvent
    public void onComputeVisibility(ComputeVisibilityEvent event) {
        event.getSetVisibility().setAllVisible(true);
    }
}
