package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.asm.events.HurtCamEffectEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class AntiHurtCamMod extends ToggleMod {
    public AntiHurtCamMod() {
        super(Category.PLAYER, "AntiHurtcam", false, "Removes hurt camera effect");
    }

    @Subscribe
    public void onHurtCamEffect(HurtCamEffectEvent event) {
        event.setCanceled(true);
    }
}
