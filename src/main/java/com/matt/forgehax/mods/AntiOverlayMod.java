package com.matt.forgehax.mods;

import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiOverlayMod extends ToggleMod {
    public AntiOverlayMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    /**
     * Disables water/lava fog
     */
    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        if(event.getState().getMaterial().equals(Material.WATER) ||
                event.getState().getMaterial().equals(Material.LAVA)) {
            event.setDensity(0);
            event.setCanceled(true);
        }
    }

    /**
     * Disables screen overlays
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderBlockOverlayEvent event) {
        event.setCanceled(true);
    }
}
