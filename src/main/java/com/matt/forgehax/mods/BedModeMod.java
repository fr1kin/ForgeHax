package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BedModeMod extends ToggleMod {
    public BedModeMod() {
        super("BedMode", false, "Sleep walking");
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        WRAPPER.getLocalPlayer().sleeping = false;
        WRAPPER.getLocalPlayer().sleepTimer = 0;
    }

    @SubscribeEvent
    public void onGuiUpdate(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiSleepMP) {
            event.setCanceled(true);
        }
    }
}
