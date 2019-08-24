package com.matt.forgehax.gui.windows;

import static com.matt.forgehax.Globals.MC;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import java.io.IOException;
import net.minecraft.client.gui.ScaledResolution;

/** Created by Babbaj on 9/5/2017. */
public abstract class GuiWindow {

  public boolean isHidden; // whether or not not to show everything below the header

  public String title;

  public int posX, headerY, windowY;
  public int bottomX, bottomY;

  // coords of where the window is being dragged from
  public int dragX, dragY;

  private boolean dragging;

  final int maxHeight =
      (int)
          (ClickGui.scaledRes.getScaledHeight()
              * 0.8); // a window can only take up 80% of the height of the window
  public int width, height; // width of the window

  public GuiWindow(String titleIn) {
    this.title = titleIn;
    width = SurfaceHelper.getTextWidth(title) + 15;
  }

  public void setPosition(int x, int y) {
    this.posX = x;
    this.headerY = y;
  }

  public String getTitle() {
    return title;
  }

  public boolean isMouseInHeader(int mouseX, int mouseY) {
    return (mouseX > posX && mouseX < posX + width && mouseY > headerY && mouseY < headerY + 20);
  }

  /** 0 == Left Click 1 == Right Click 2 == Middle Click */
  public void mouseClicked(int mouseX, int mouseY, int state) {
    if (state != 0) return;
    if (isMouseInHeader(mouseX, mouseY)) {
      dragging = true;

      dragX = mouseX - posX;
      dragY = mouseY - headerY;
    }
  }

  public void mouseReleased(int x, int y, int state) {
    dragging = false;
  }

  public void handleMouseInput() throws IOException {
    // scrolling
  }

  public void keyTyped(char typedChar, int keyCode) throws IOException {
    // text input
  }

  public void drawWindow(int mouseX, int mouseY) {
    ClickGui.scaledRes = new ScaledResolution(MC);
    if (dragging) {
      posX = mouseX - dragX;
      headerY = mouseY - dragY;
    }
    drawHeader();
    windowY = headerY + 21;
    SurfaceHelper.drawOutlinedRectShaded(
        posX, windowY, width, height, Utils.toRGBA(130, 130, 130, 255), 80, 3);
  }

  public void drawTooltip(int mouseX, int mouseY) {}

  public void drawHeader() {
    // draw the title of the window
    SurfaceHelper.drawOutlinedRectShaded(
        posX, headerY, width, 20, Utils.toRGBA(150, 150, 150, 255), 50, 5);
    SurfaceHelper.drawTextShadowCentered(
        getTitle(), posX + width / 2f, headerY + 10, Utils.toRGBA(255, 255, 255, 255));
  }
}
