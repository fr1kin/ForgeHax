package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiFireMod extends ToggleMod {
    public AntiFireMod() {
        super("AntiFire", false, "Removes fire");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        event.getEntityLiving().extinguish();
    }
}
