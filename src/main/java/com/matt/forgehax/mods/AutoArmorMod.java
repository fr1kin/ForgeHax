package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;

public class AutoArmorMod extends ToggleMod {
  public AutoArmorMod() {
    super(Category.COMBAT, "AutoArmor", false, "Automatically put on armor");
  }
}
