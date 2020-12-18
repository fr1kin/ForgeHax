package dev.fiki.forgehax.main.mods.world;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.render.CullCavesEvent;

import static dev.fiki.forgehax.main.Common.reloadChunkSmooth;

@RegisterMod(
    name = "NoCaveCulling",
    description = "Disables mojangs dumb cave culling shit",
    category = Category.WORLD
)
public class NoCaveCulling extends ToggleMod {

  @Override
  public void onEnabled() {
    reloadChunkSmooth();
  }

  @Override
  public void onDisabled() {
    reloadChunkSmooth();
  }

  @SubscribeListener
  public void onCullCaves(CullCavesEvent event) {
    event.setCanceled(true);
  }
}
