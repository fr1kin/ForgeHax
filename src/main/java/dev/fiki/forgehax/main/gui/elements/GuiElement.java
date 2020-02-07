package dev.fiki.forgehax.main.gui.elements;

import dev.fiki.forgehax.main.gui.windows.GuiWindowSetting;
import dev.fiki.forgehax.main.util.mod.ToggleMod;

/**
 * Created by Babbaj on 9/6/2017.
 */
public class GuiElement {
  
  public GuiWindowSetting parentWindow;
  
  public int width, height; // width and height of the element
  public int subX, subY; // coords of the element relative to the parent window
  public int x, y; // coords of the element posX + subX
  
  public ToggleMod mod;
  
  public GuiElement(ToggleMod settingIn, GuiWindowSetting parent) {
    this.parentWindow = parent;
    this.mod = settingIn;
  }
  
  public void mouseClicked(int x, int y, int state) {
  }
  
  public void mouseReleased(int x, int y, int state) {
  }
  
  public void keyTyped(char typedChar, int keyCode) {
  }
  
  public void draw(int mouseX, int mouseY) {
    this.x = getPosX() + this.subX + 1;
    this.y = getPosY() + this.subY + 21;
  }
  
  public int getPosX() {
    return (int) parentWindow.posX;
  }
  
  public int getPosY() {
    return (int) parentWindow.headerY;
  }
  
  public boolean isMouseInElement(int mouseX, int mouseY) {
    return mouseX > this.x
      && mouseX < this.x + width
      && mouseY > this.y
      && mouseY < this.y + height;
  }
}
