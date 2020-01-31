package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.ForgeHaxHooks;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;

/**
 * Created by Babbaj on 9/2/2017.
 */
@RegisterMod
public class ExtraTab extends ToggleMod {
  
  public ExtraTab() {
    super(Category.MISC, "ExtraTab", false, "Increase max size of tab list");
  }
  
  @Override
  public void onEnabled() {
    ForgeHaxHooks.doIncreaseTabListSize = true;
  }
  
  @Override
  public void onDisabled() {
    ForgeHaxHooks.doIncreaseTabListSize = false;
  }
}
