package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.material.Material;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiOverlayMod extends ToggleMod {
    public AntiOverlayMod() {
        super("AntiOverlay", false, "Removes screen overlays");
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
