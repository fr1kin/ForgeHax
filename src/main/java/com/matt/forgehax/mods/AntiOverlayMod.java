package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.advancements.GuiAdvancement;
import net.minecraftforge.client.event.*;
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
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) { event.setCanceled(true); }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HELMET) ||
                event.getType().equals(RenderGameOverlayEvent.ElementType.PORTAL))
            event.setCanceled(true);
    }


}
