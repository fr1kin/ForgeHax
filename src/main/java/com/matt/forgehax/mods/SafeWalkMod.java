package com.matt.forgehax.mods;

import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

/** Created on 9/4/2016 by fr1kin */
@RegisterMod
public class SafeWalkMod extends ToggleMod {
  public SafeWalkMod() {
    super(Category.PLAYER, "SafeWalk", false, "Prevents you from falling off blocks");
  }

  @Override
  public void onEnabled() {
    ForgeHaxHooks.isSafeWalkActivated = true;
  }

  @Override
  public void onDisabled() {
    ForgeHaxHooks.isSafeWalkActivated = false;
  }
}
