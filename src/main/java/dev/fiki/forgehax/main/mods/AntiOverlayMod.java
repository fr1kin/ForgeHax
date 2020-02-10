package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.*;

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
    if (isInWorld() && (getLocalPlayer().isInLava() || getLocalPlayer().isInWater())) {
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
    ItemStack stack = FastReflection.Fields.GameRenderer_itemActivationItem.get(getGameRenderer());

    if(stack != null && Items.TOTEM_OF_UNDYING.equals(stack.getItem())) {
      FastReflection.Fields.GameRenderer_itemActivationItem.set(getGameRenderer(), null);
    }
  }
}
