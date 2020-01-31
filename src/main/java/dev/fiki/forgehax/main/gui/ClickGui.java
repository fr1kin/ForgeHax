package dev.fiki.forgehax.main.gui;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.gui.windows.GuiWindow;
import dev.fiki.forgehax.main.gui.windows.GuiWindowMod;
import dev.fiki.forgehax.main.util.mod.Category;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class ClickGui extends Screen implements Globals {
  
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
    super(new StringTextComponent("ForgeHax GUI"));
    // set initial window positions
    // TODO: load from settings
    // TODO: improve this a bit maybe
    for (int i = 0; i < windowList.size(); i++) {
      int x = (i + 1) * Globals.getScreenWidth() / (windowList.size() + 1)
          - windowList.get(i).width / 2
          - 10;
      int y = Globals.getScreenHeight() / 15;
      windowList.get(i).setPosition(x, y);
    }
  }
  
  public static ClickGui getInstance() {
    return (INSTANCE == null) ? (INSTANCE = new ClickGui()) : INSTANCE;
  }
  
  public void moveWindowToTop(GuiWindow window) {
    if (windowList.remove(window)) // if it wasnt already in the list dont add it
    {
      windowList.add(window);
    }
  }
  
  public boolean isMouseInWindow(double mouseX, double mouseY, GuiWindow window) {
    return mouseX > window.posX
      && mouseX < window.bottomX
      && mouseY > window.headerY
      && mouseY < window.bottomY;
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    // super.render(mouseX, mouseY, partialTicks);

    for (GuiWindow window : windowList) {
      window.drawWindow(mouseX, mouseY);
    }

    for (GuiWindow window : windowList) {
      window.drawTooltip(mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int buttonCode) {
    for (GuiWindow window : Lists.reverse(windowList)) {
      if (isMouseInWindow(mouseX, mouseY, window)) {
        window.mouseClicked(mouseX, mouseY, buttonCode);
        moveWindowToTop(window);
        break;
      }
    }

    return super.mouseClicked(mouseX, mouseY, buttonCode);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    for (GuiWindow window : windowList) {
      window.mouseReleased(mouseX, mouseY, state);
    }

    return super.mouseReleased(mouseX, mouseY, state);
  }

  @Override
  public void mouseMoved(double mouseX, double mouseY) {
    int scale = (int) Globals.getMainWindow().getGuiScaleFactor();
    for (GuiWindow window : Lists.reverse(windowList)) {
      if (isMouseInWindow(
          mouseX / scale, (Globals.getScreenHeight() - mouseY) / scale, window)) {
        window.handleMouseInput(mouseX, mouseY);
        break;
      }
    }
  }

  @Override
  public boolean mouseScrolled(double handle, double scrollX, double scrollY) {
    // TODO: scrolling for handleMouseInput
    return super.mouseScrolled(handle, scrollX, scrollY);
  }

  @Override
  public boolean charTyped(char character, int keyCode) {
    boolean ret = super.charTyped(character, keyCode);

    for (GuiWindow window : windowList) {
      window.keyTyped(character, keyCode);
    }

    return ret;
  }
}
