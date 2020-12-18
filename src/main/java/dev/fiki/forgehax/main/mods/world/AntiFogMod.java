package dev.fiki.forgehax.main.mods.world;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.FogDensityRenderEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;

@RegisterMod(
    name = "AntiFog",
    description = "Removes fog",
    category = Category.WORLD
)
public class AntiFogMod extends ToggleMod {
  @SubscribeListener
  public void onFogDensity(FogDensityRenderEvent event) {
    event.setDensity(0);
    event.setCanceled(true);
  }
}
