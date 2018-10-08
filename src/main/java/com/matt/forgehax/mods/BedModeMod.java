package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.asm.events.replacementhooks.GuiOpenEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiSleepMP;

import static com.matt.forgehax.Helper.getLocalPlayer;

@RegisterMod
public class BedModeMod extends ToggleMod {
    public BedModeMod() {
        super(Category.PLAYER, "BedMode", false, "Sleep walking");
    }

    @Subscribe
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        FastReflection.Fields.EntityPlayer_sleeping.set(getLocalPlayer(), false);
        FastReflection.Fields.EntityPlayer_sleepTimer.set(getLocalPlayer(), 0);
    }

    @Subscribe
    public void onGuiUpdate(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiSleepMP) {
            event.setCanceled(true);
        }
    }
}
