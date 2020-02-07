package dev.fiki.forgehax.main.gui.windows;

import dev.fiki.forgehax.main.gui.ClickGui;
import dev.fiki.forgehax.main.gui.elements.GuiElement;
import dev.fiki.forgehax.main.gui.elements.GuiTextInput;
import dev.fiki.forgehax.main.util.mod.AbstractMod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowSetting extends GuiWindow {
  
  public List<GuiElement> inputList =
    new ArrayList<>(); // list of toggles, sliders, text inputs, etc.
  
  private AbstractMod mod;
  
  public GuiWindowSetting(AbstractMod modIn, int x, int y) {
    super(modIn.getName() + " Settings");
    this.mod = modIn;
    initializeInputs();
  }
  
  private void initializeInputs() {
    // Map<Object, String> settingMap;
    // inputList.add(new GuiSlider());
    inputList.add(new GuiTextInput(null, this));
    height += 13;
  }
  
  public String getModName() {
    return mod.getName();
  }
  
  public AbstractMod getMod() {
    return this.mod;
  }
  
  public void drawWindow(int mouseX, int mouseY) {
    super.drawWindow(mouseX, mouseY);
    
    for (GuiElement input : inputList) {
      input.x = 2;
      input.y = height + 2;
      input.width = width;
      input.draw(mouseX, mouseY);
    }
    
    // update variables
    bottomX = posX + width; // set the coords of the bottom right corner for mouse coord testing
    bottomY = windowY + height;
  }
  
  public void keyTyped(char typedChar, int keyCode) {
    for (GuiElement element : inputList) {
      element.keyTyped(typedChar, keyCode);
    }
  }
  
  public void mouseClicked(double x, double y, int state) {
    super.mouseClicked(x, y, state);
    if (state == 2 && isMouseInHeader(x, y)) { // delete the window on middle click
      ClickGui.getInstance().windowList.remove(this);
    } else {
      for (GuiElement input : inputList) {
        // if (isMouseInElement(x , y, input))
        input.mouseClicked((int)x, (int)y, state);
      }
    }
  }
  
  public void mouseReleased(double x, double y, int state) {
    super.mouseReleased(x, y, state);
    for (GuiElement input : inputList) {
      input.mouseReleased((int)x, (int)y, state);
    }
  }
}
