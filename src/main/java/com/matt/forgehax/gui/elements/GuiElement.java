package com.matt.forgehax.gui.elements;

import com.matt.forgehax.gui.windows.GuiWindowSetting;
import com.matt.forgehax.util.command.Setting;
import java.io.IOException;

/**
 * Created by Babbaj on 9/6/2017.
 */
public class GuiElement {
  
  public GuiWindowSetting parentWindow;
  
  public int width, height; // width and height of the element
  public int subX, subY; // coords of the element relative to the parent window
  public int x, y; // coords of the element posX + subX
  
  public Setting setting;
  
  public GuiElement(Setting settingIn, GuiWindowSetting parent) {
    this.parentWindow = parent;
    this.setting = settingIn;
  }
  
  public void mouseClicked(int x, int y, int state) {
  }
  
  public void mouseReleased(int x, int y, int state) {
  }
  
  public void keyTyped(char typedChar, int keyCode) throws IOException {
  }
  
  public void draw(int mouseX, int mouseY) {
    this.x = getPosX() + this.subX + 1;
    this.y = getPosY() + this.subY + 21;
  }
  
  public int getPosX() {
    return parentWindow.posX;
  }
  
  public int getPosY() {
    return parentWindow.headerY;
  }
  
  public boolean isMouseInElement(int mouseX, int mouseY) {
    return mouseX > this.x
      && mouseX < this.x + width
      && mouseY > this.y
      && mouseY < this.y + height;
  }
}
