package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    // TODO: 1.15 make sure this hides liquid fog properly
    if (Common.isInWorld() && (Common.getLocalPlayer().isInLava() || Common.getLocalPlayer().isInWater())) {
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
    // TODO: 1.15 find a new way to remove this overlay
  }
}
