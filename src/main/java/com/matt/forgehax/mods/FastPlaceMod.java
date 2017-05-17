package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 9/4/2016 by fr1kin
 */

@RegisterMod
public class FastPlaceMod extends ToggleMod {
    public FastPlaceMod() {
        super("FastPlace", false, "Fast place");
    }

    @SubscribeEvent
    public void onUpdate(LocalPlayerUpdateEvent event) {
        MC.rightClickDelayTimer = 0;
    }
}
