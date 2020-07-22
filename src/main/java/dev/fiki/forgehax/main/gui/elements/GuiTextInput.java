package dev.fiki.forgehax.main.gui.elements;

import dev.fiki.forgehax.main.gui.windows.GuiWindowSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

/**
 * Created by Babbaj on 9/15/2017.
 */
public class GuiTextInput extends GuiElement {
  
  private final int blinkSpeed = 30; // how often to blink the thing
  private int ticks;
  private boolean isActive;
  
  private int selectedIndex = -1;
  private StringBuilder input = new StringBuilder();
  
  public GuiTextInput(ToggleMod settingIn, GuiWindowSetting parent) {
    super(settingIn, parent);
    height = 12;
  }
  
  public void mouseClicked(int mouseX, int mouseY, int state) {
    isActive = isMouseInElement(mouseX, mouseY);
  }
  
  public void keyTyped(char typedChar, int keyCode) {
    if (isActive) {
      switch (keyCode) {
        case GLFW.GLFW_KEY_ESCAPE:
          isActive = false;
          break;
        
        case GLFW.GLFW_KEY_ENTER:
          isActive = false;
          // setValue(input);
          getLocalPlayer().sendMessage(new StringTextComponent(input.toString()), null);
          break;
        
        case GLFW.GLFW_KEY_BACKSPACE:
          if (selectedIndex > -1) {
            input.deleteCharAt(selectedIndex);
            selectedIndex--;
          }
          break;
        
        case GLFW.GLFW_KEY_LEFT:
          selectedIndex--;
          break;
        
        case GLFW.GLFW_KEY_RIGHT:
          selectedIndex++;
          break;
        
        default:
          if (isValidChar(typedChar)) {
            selectedIndex++;
            input.insert(selectedIndex, typedChar);
          }
      }
      selectedIndex = MathHelper.clamp(selectedIndex, -1, input.length() - 1);
    }
  }
  
  public void draw(int mouseX, int mouseY) {
    super.draw(x, y);
    SurfaceHelper.drawRect(x, y, width - 2, height, Colors.WHITE.toBuffer());
    SurfaceHelper.drawOutlinedRect(x, y, width - 2, height, Colors.BLACK.toBuffer());
    if (ticks % blinkSpeed * 2 > blinkSpeed && isActive) {
      int width = getBlinkWidth();
      // SurfaceHelper.drawLine(x+width+1, y+2,x+width+1, y+height-2, Utils.Colors.BLACK);
    }
    SurfaceHelper.drawText(getInputString(), x + 1, y + 2, Colors.BLACK.toBuffer());
    
    ticks++;
  }
  
  private int getBlinkWidth() {
    if (input.length() > 0) {
      return SurfaceHelper.getTextWidth(input.substring(0, selectedIndex + 1));
    } else {
      return 0;
    }
  }
  
  private String getInputString() {
    return input.toString();
  }
  
  private boolean isValidChar(char charIn) {
    return !Character.isISOControl(charIn);
  }
  
  private void setValue(String in) {
    mod.getEnabledSetting().setValueRaw(in);
  }
}
