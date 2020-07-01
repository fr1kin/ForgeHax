package com.matt.forgehax.gui;

import static com.matt.forgehax.Helper.getModManager;

import com.google.common.collect.Lists;
import com.matt.forgehax.Globals;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.mods.services.GuiService;
import com.matt.forgehax.mods.services.GuiService.WindowPosition;
import com.matt.forgehax.util.mod.Category;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class ClickGui extends GuiScreen implements Globals {
  
  private static ClickGui INSTANCE;
  
  public final List<GuiWindow> windowList = new ArrayList<>();
  
  private GuiWindowMod combatWindow = new GuiWindowMod(Category.COMBAT);
  private GuiWindowMod movementWindow = new GuiWindowMod(Category.MOVEMENT);
  private GuiWindowMod playerWindow = new GuiWindowMod(Category.PLAYER);
  private GuiWindowMod renderWindow = new GuiWindowMod(Category.RENDER);
  private GuiWindowMod worldWindow = new GuiWindowMod(Category.WORLD);
  private GuiWindowMod miscWindow = new GuiWindowMod(Category.MISC);
  private GuiWindowMod chatWindow = new GuiWindowMod(Category.CHAT);
  private final GuiWindowMod guiWindow = new GuiWindowMod(Category.GUI);
  
  {
    windowList.add(playerWindow);
    windowList.add(movementWindow);
    windowList.add(chatWindow);
    windowList.add(renderWindow);
    windowList.add(combatWindow);
    windowList.add(worldWindow);
    windowList.add(guiWindow);
    windowList.add(miscWindow);
  }
  
  public static ScaledResolution scaledRes = new ScaledResolution(MC);
  
  public int baseColor;
  
  private ClickGui() {
    // set initial window positions
    // TODO: improve this a bit maybe
    for (int i = 0; i < windowList.size(); i++) {
      // Look up a saved position
      final String title = windowList.get(i).title;
      Tuple<Integer,Integer> pos = getModManager().get(GuiService.class).get().windows.stream()
        .filter(w -> w.getUniqueHeader().equals(title))
        .map(w -> new Tuple<Integer,Integer>(w.x, w.y))
        .findFirst()
        .orElse(null);

      if (pos != null) {
        windowList.get(i).setPosition(pos.getFirst(), pos.getSecond());
        continue; // Go to next one
      }

      // Calculate fresh if none is found
      final int x = (i + 3) / 2 * scaledRes.getScaledWidth() / (windowList.size() - 2)
          - windowList.get(i).width / 2;
      final int y = scaledRes.getScaledHeight() / 25 + order(i) * scaledRes.getScaledHeight() / 2;

      // Here check if the window goes offscreen, if true push it down all the others
      windowList.get(i).setPosition(x, y);
      getModManager().get(GuiService.class).get().windows
                  .add(new WindowPosition(windowList.get(i).title, x, y));
    }
  }

  private int order(final int i) {
    if(i < 2) {
      return 0;
    }
    return (i + 1) % 2; // Distance between windows
  }

  public static ClickGui getInstance() {
    return (INSTANCE == null) ? (INSTANCE = new ClickGui()) : INSTANCE;
  }
  
  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
  
  @SubscribeEvent
  public void onScreenUpdated(GuiScreenEvent.InitGuiEvent.Post ev) {
    scaledRes = new ScaledResolution(MC);
  }
  
  public void moveWindowToTop(GuiWindow window) {
    if (windowList.remove(window)) // if it wasnt already in the list dont add it
    {
      windowList.add(window);
    }
  }
  
  public boolean isMouseInWindow(int mouseX, int mouseY, GuiWindow window) {
    if (window.isHidden) {
      return mouseX > window.posX
        && mouseX < window.bottomX
        && mouseY > window.headerY
        && mouseY < window.headerY + 20;
    }
    return mouseX > window.posX
      && mouseX < window.bottomX
      && mouseY > window.headerY
      && mouseY < window.bottomY;
  }
  
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    for (GuiWindow window : windowList) {
      window.drawWindow(mouseX, mouseY);
    }
    
    for (GuiWindow window : windowList) {
      window.drawTooltip(mouseX, mouseY);
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
  
  public void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);
    for (GuiWindow window : windowList) {
      window.keyTyped(typedChar, keyCode);
    }
  }
  
  public void handleMouseInput() throws IOException {
    // used for scrolling
    super.handleMouseInput();
    
    int scale = scaledRes.getScaleFactor();
    for (GuiWindow window : Lists.reverse(windowList)) {
      if (isMouseInWindow(
        Mouse.getEventX() / scale, (MC.displayHeight - Mouse.getEventY()) / scale, window)) {
        window.handleMouseInput();
        break;
      }
    }
  }
}
