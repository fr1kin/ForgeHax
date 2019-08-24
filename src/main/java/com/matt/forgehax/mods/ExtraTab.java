package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

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
