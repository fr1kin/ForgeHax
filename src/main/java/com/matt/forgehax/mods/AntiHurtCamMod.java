package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.HurtCamEffectEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiHurtCamMod extends ToggleMod {
    public AntiHurtCamMod() {
        super("AntiHurtcam", false, "Removes hurt camera effect");
    }

    @SubscribeEvent
    public void onHurtCamEffect(HurtCamEffectEvent event) {
        event.setCanceled(true);
    }
}
