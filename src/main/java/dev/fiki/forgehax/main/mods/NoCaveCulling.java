package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

  @SubscribeEvent
  public void onCullCaves(CullCavesEvent event) {
    event.setCanceled(true);
  }
}
