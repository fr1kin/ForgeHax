package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class AutoPitch extends ToggleMod {
  
  public AutoPitch() {
    super(Category.PLAYER, "AutoPitch", false, "Automatically sets pitch to best trajectory");
  }
  
  // TODO: reimplement
}
