package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod
public class NoCaveCulling extends ToggleMod {

  public NoCaveCulling() {
    super(Category.RENDER, "NoCaveCulling", false, "Disables mojangs dumb cave culling shit");
  }

  @Override
  public void onEnabled() {
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable("NoCaveCulling");
    reloadChunkSmooth();
  }

  @Override
  public void onDisabled() {
    ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable("NoCaveCulling");
    reloadChunkSmooth();
  }
}
