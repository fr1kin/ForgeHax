package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiOverlayMod extends ToggleMod {
  
  public AntiOverlayMod() {
    super(Category.PLAYER, "AntiOverlay", false, "Removes screen overlays");
  }
  
  /**
   * Disables water/lava fog
   */
  @SubscribeEvent
  public void onFogRender(EntityViewRenderEvent.FogDensity event) {
    if (event.getState().getMaterial().equals(Material.WATER)
      || event.getState().getMaterial().equals(Material.LAVA)) {
      event.setDensity(0);
      event.setCanceled(true);
    }
  }
  
  /**
   * Disables screen overlays
   */
  @SubscribeEvent
  public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
    event.setCanceled(true);
  }
  
  @SubscribeEvent
  public void onRenderGameOverlay(RenderGameOverlayEvent event) {
    if (event.getType().equals(RenderGameOverlayEvent.ElementType.HELMET)
      || event.getType().equals(RenderGameOverlayEvent.ElementType.PORTAL)) {
      event.setCanceled(true);
    }
  }
  
  @SubscribeEvent
  public void onRender(RenderEvent event) {
    ItemStack item = FastReflection.Fields.EntityRenderer_itemActivationItem.get(MC.entityRenderer);
  
    if (item != null && item.getItem() == Items.TOTEM_OF_UNDYING) {
      FastReflection.Fields.EntityRenderer_itemActivationItem.set(MC.entityRenderer, null);
    }
  }
}
