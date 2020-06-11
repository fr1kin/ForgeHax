package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.mod.EntityCoords;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class LightLevel extends EntityCoords {

  public LightLevel() {
    super("LightLevel", "Shows the light level of the block you're currently standing on"); }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  public String getInfoDisplayText() {
    return "Light: " + MC.world.getLight(getPosition());
  }
}
