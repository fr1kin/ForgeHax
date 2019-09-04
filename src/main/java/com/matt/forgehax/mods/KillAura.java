package com.matt.forgehax.mods;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import net.minecraft.entity.Entity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class KillAura extends ToggleMod {
  
  enum TargetModes {
    CLOSEST,
    CROSSHAIR
  }
  
  private TargetModes modes = TargetModes.CLOSEST;
  
  public KillAura() {
    super(Category.COMBAT, "KillAura", false, "Attack anything within given parameters");
  }
  
  private Entity getTarget() {
    return null;
  }
}
