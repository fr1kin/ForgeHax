package dev.fiki.forgehax.main.gui.elements;

import dev.fiki.forgehax.main.util.mod.AbstractMod;
import dev.fiki.forgehax.main.util.color.Color;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiButton {
  
  private final AbstractMod mod;
  
  private static final int COLOR_ENABLED = Color.of(65, 65, 65, 200).toBuffer();
  private static final int COLOR_DISABLED = Color.of(100, 100, 100, 150).toBuffer();
  
  public int width;
  public static final int height = 15;
  
  public int x, y; // used to get the area they can be clicked in
  
  public GuiButton(AbstractMod modIn) {
    this.mod = modIn;
  }
  
  public void setCoords(int xIn, int yIn) {
    this.x = xIn;
    this.y = yIn;
  }
  
  public boolean isModEnabled() {
    return mod.isEnabled();
  }
  
  public void toggleMod() {
    if (!mod.isEnabled()) {
      mod.enable();
    } else {
      mod.disable();
    }
  }
  
  public String getName() {
    return mod.getName();
  }
  
  public AbstractMod getMod() {
    return this.mod;
  }
  
  public int getColor() {
    return isModEnabled() ? COLOR_ENABLED : COLOR_DISABLED;
  }
}
