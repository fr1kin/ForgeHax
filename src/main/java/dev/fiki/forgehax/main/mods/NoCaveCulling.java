package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.common.StateManager;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

import static dev.fiki.forgehax.main.Common.reloadChunkSmooth;

@RegisterMod
public class NoCaveCulling extends ToggleMod {

  private final StateManager.StateHandle disableCulling =
      ForgeHaxHooks.HOOK_shouldDisableCaveCulling.createHandle(NoCaveCulling.class);

  public NoCaveCulling() {
    super(Category.RENDER, "NoCaveCulling", false, "Disables mojangs dumb cave culling shit");
  }

  @Override
  public void onEnabled() {
    disableCulling.enable();
    reloadChunkSmooth();
  }

  @Override
  public void onDisabled() {
    disableCulling.disable();
    reloadChunkSmooth();
  }
}
