package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Created on 5/16/2017 by fr1kin
 */

@RegisterMod
public class NoWeather extends ToggleMod {
    public NoWeather() {
        super("NoWeather", false, "Disables weather");
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        event.world.setRainStrength(0);
    }
}
