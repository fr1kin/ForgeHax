package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class AntiFireMod extends ToggleMod {
    public AntiFireMod() {
        super(Category.PLAYER, "AntiFire", false, "Removes fire");
    }

    @Subscribe
    public void onUpdate(LocalPlayerUpdateEvent event) {
        event.getEntity().extinguish();
    }
}
