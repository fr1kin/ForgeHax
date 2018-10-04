package com.matt.forgehax.gui.elements;

import static com.matt.forgehax.Globals.MC;

import com.matt.forgehax.gui.windows.GuiWindowSetting;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

/** Created by Babbaj on 9/15/2017. */
public class GuiTextInput extends GuiElement {

  private final int blinkSpeed = 30; // how often to blink the thing
  private int ticks;
  private boolean isActive;

  private int selectedIndex = -1;
  private StringBuilder input = new StringBuilder();

  public GuiTextInput(Setting settingIn, GuiWindowSetting parent) {
    super(settingIn, parent);
    height = 12;
  }

  public void mouseClicked(int mouseX, int mouseY, int state) {
    isActive = isMouseInElement(mouseX, mouseY);
  }

  public void keyTyped(char typedChar, int keyCode) throws IOException {
    if (isActive) {
      switch (keyCode) {
        case Keyboard.KEY_ESCAPE:
          isActive = false;
          break;

        case Keyboard.KEY_RETURN:
          isActive = false;
          // setValue(input);
          MC.player.sendMessage(new TextComponentString(input.toString()));
          break;

        case Keyboard.KEY_BACK:
          if (selectedIndex > -1) {
            input.deleteCharAt(selectedIndex);
            selectedIndex--;
          }
          break;

        case Keyboard.KEY_LEFT:
          selectedIndex--;
          break;

        case Keyboard.KEY_RIGHT:
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
    SurfaceHelper.drawRect(x, y, width - 2, height, Utils.Colors.WHITE);
    SurfaceHelper.drawOutlinedRect(x, y, width - 2, height, Utils.Colors.BLACK);
    if (ticks % blinkSpeed * 2 > blinkSpeed && isActive) {
      int width = getBlinkWidth();
      // SurfaceHelper.drawLine(x+width+1, y+2,x+width+1, y+height-2, Utils.Colors.BLACK);
    }
    SurfaceHelper.drawText(getInputString(), x + 1, y + 2, Utils.Colors.BLACK);

    ticks++;
  }

  private int getBlinkWidth() {
    if (input.length() > 0)
      return SurfaceHelper.getTextWidth(input.substring(0, selectedIndex + 1));
    else return 0;
  }

  private String getInputString() {
    return input.toString();
  }

  private boolean isValidChar(char charIn) {
    return !Character.isISOControl(charIn);
  }

  private void setValue(String in) {
    setting.set(in);
  }
}
