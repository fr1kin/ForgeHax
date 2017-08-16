package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static com.matt.forgehax.Helper.*;

@RegisterMod
public class BedModeMod extends ToggleMod {
    public BedModeMod() {
        super("BedMode", false, "Sleep walking");
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        FastReflection.Fields.EntityPlayer_sleeping.set(getLocalPlayer(), false);
        FastReflection.Fields.EntityPlayer_sleepTimer.set(getLocalPlayer(), 0);
    }

    @SubscribeEvent
    public void onGuiUpdate(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiSleepMP) {
            event.setCanceled(true);
        }
    }
}
