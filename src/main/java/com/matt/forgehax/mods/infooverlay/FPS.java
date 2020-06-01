package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.Minecraft;

@RegisterMod
public class FPS extends ToggleMod {

  public FPS() {
    super(Category.GUI, "FPS", true, "Shows your current FPS");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  /*@Override
  public boolean isVisible() { return false; }*/

  public String getInfoDisplayText() {
    return "FPS: " + String.format("%s", Minecraft.getDebugFPS());
  }
}
