package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.Globals;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.util.mod.Category;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;

/** Created by Babbaj on 9/5/2017. */
public class ClickGui extends GuiScreen implements Globals {

  private static ClickGui INSTANCE;

  public final List<GuiWindow> windowList = new ArrayList<>();

  private GuiWindowMod combatWindow = new GuiWindowMod(Category.COMBAT);
  private GuiWindowMod playerWindow = new GuiWindowMod(Category.PLAYER);
  private GuiWindowMod renderWindow = new GuiWindowMod(Category.RENDER);
  private GuiWindowMod worldWindow = new GuiWindowMod(Category.WORLD);
  private GuiWindowMod miscWindow = new GuiWindowMod(Category.MISC);

  {
    windowList.add(combatWindow);
    windowList.add(playerWindow);
    windowList.add(renderWindow);
    windowList.add(worldWindow);
    windowList.add(miscWindow);
  }

  public int baseColor;

  private ClickGui() {
    // set initial window positions
    // TODO: load from settings
    // TODO: improve this a bit maybe
    for (int i = 0; i < windowList.size(); i++) {
      int x =
          (i + 1) * MC.mainWindow.getScaledWidth() / (windowList.size() + 1)
              - windowList.get(i).width / 2
              - 10;
      int y = MC.mainWindow.getScaledHeight() / 15;
      windowList.get(i).setPosition(x, y);
    }
  }

  public static ClickGui getInstance() {
    return (INSTANCE == null) ? (INSTANCE = new ClickGui()) : INSTANCE;
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  public void moveWindowToTop(GuiWindow window) {
    if (windowList.remove(window)) // if it wasnt already in the list dont add it
    windowList.add(window);
  }

  public boolean isMouseInWindow(int mouseX, int mouseY, GuiWindow window) {
    return mouseX > window.posX
        && mouseX < window.bottomX
        && mouseY > window.headerY
        && mouseY < window.bottomY;
  }

  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    for (GuiWindow window : windowList) {
      window.drawWindow(mouseX, mouseY);
    }
  }

  public void mouseClicked(int mouseX, int mouseY, int b) throws IOException {
    try {
      for (GuiWindow window : Lists.reverse(windowList)) {
        if (isMouseInWindow(mouseX, mouseY, window)) {
          window.mouseClicked(mouseX, mouseY, b);
          moveWindowToTop(window);
          return;
        }
      }
      super.mouseClicked(mouseX, mouseY, b);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void mouseReleased(int x, int y, int state) {
    for (GuiWindow window : windowList) {
      window.mouseReleased(x, y, state);
    }
    super.mouseReleased(x, y, state);
  }

  // TODO: update
  /*public void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    for (GuiWindow window : windowList) {
      window.keyTyped(typedChar, keyCode);
    }
  }*/

  // TODO: update
  /*public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
    // used for scrolling

    int scale = MC.mainWindow.getScaleFactor();
    for (GuiWindow window : Lists.reverse(windowList)) {
      if (isMouseInWindow(
          Mouse.getEventX() / scale, (MC.displayHeight - Mouse.getEventY()) / scale, window)) {
        window.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
        break;
      }
    }
  }*/
}
