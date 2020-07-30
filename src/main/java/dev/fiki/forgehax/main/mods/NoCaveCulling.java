package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.reloadChunkSmooth;

@RegisterMod
public class NoCaveCulling extends ToggleMod {

  public NoCaveCulling() {
    super(Category.RENDER, "NoCaveCulling", false, "Disables mojangs dumb cave culling shit");
  }

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
