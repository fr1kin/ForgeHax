package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiFogMod extends ToggleMod {
    public AntiFogMod() {
        super("AntiFog", false, "Removes fog");
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        event.setDensity(0);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFogColor(EntityViewRenderEvent.FogColors event) {
        event.setRed(55);
        event.setGreen(55);
        event.setBlue(55);
    }
}
