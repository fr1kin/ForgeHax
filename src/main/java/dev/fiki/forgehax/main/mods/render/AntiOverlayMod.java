package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.BlockOverlayRenderEvent;
import dev.fiki.forgehax.api.events.render.FogDensityRenderEvent;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "AntiOverlay",
    description = "Removes screen overlays",
    category = Category.RENDER
)
@RequiredArgsConstructor
public class AntiOverlayMod extends ToggleMod {
  @MapField(parentClass = GameRenderer.class, value = "itemActivationItem")
  private final ReflectionField<ItemStack> GameRenderer_itemActivationItem;

  /**
   * Disables water/lava fog
   */
  @SubscribeListener
  public void onFogRender(FogDensityRenderEvent event) {
    if (isInWorld() && (getLocalPlayer().isInLava() || getLocalPlayer().isInWater())) {
      event.setDensity(0);
      event.setCanceled(true);
    }
  }

  /**
   * Disables screen overlays
   */
  @SubscribeListener
  public void onRenderBlockOverlay(BlockOverlayRenderEvent event) {
    event.setCanceled(true);
  }

  @SubscribeListener
  public void onRenderHelmetOverlay(RenderPlaneEvent.Helmet event) {
    event.setCanceled(true);
  }

  @SubscribeListener
  public void onRenderPortalOverlay(RenderPlaneEvent.Portal event) {
    event.setCanceled(true);
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    ItemStack stack = GameRenderer_itemActivationItem.get(getGameRenderer());

    if (stack != null && Items.TOTEM_OF_UNDYING.equals(stack.getItem())) {
      GameRenderer_itemActivationItem.set(getGameRenderer(), null);
    }
  }
}
