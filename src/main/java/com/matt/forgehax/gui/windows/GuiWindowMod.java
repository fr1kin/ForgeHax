package com.matt.forgehax.gui.windows;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiButton;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowMod extends GuiWindow {
  
  public List<GuiButton> buttonList = new ArrayList<>();
  
  /**
   * The button list y coord needs to be offset to move them up or down the window 0 = natural state
   * anything above 0 means the button list has moved up and the user has scrolled down
   */
  private int buttonListOffset;
  
  public Category category;
  
  // public int windowY; // Y value of the modlist - 20 pixels lower than the header Y
  
  public GuiWindowMod(Category categoryIn) {
    super(categoryIn.getPrettyName());
    category = categoryIn;
    addModsToButtonList();
  }
  
  private void addModsToButtonList() {
    int maxWidth = 0;
    int newHeight = 0;
    for (BaseMod mod : getModManager().getMods()) {
      if (mod.getModCategory().equals(category) && !mod.isHidden()) {
        GuiButton moduleButton = new GuiButton(mod);
        buttonList.add(moduleButton);
        
        newHeight += GuiButton.height + 1;
        
        String name = moduleButton.getName();
        int width = SurfaceHelper.getTextWidth(name);
        if (width > maxWidth) {
          maxWidth = width;
        }
      }
    }
    height = Math.min(maxHeight, newHeight + 3);
    width = maxWidth + 15; // set the width of window to the width of the longest mod name
  }
  
  private void drawModTooltip(BaseMod mod, int xScaled, int yScaled) {
    int scale = ClickGui.scaledRes.getScaleFactor();
    
    String modName = mod.getModName();
    String modDescription = mod.getModDescription();
    int offset = 2;
    int tooltipX = xScaled / scale + offset;
    int tooltipY = yScaled / scale + offset;
    int padding = 2;
    int tooltipWidth =
      Math.max(SurfaceHelper.getTextWidth(modName), SurfaceHelper.getTextWidth(modDescription))
        / scale + padding * 2;
    int lineHeight = SurfaceHelper.getTextHeight() / scale;
    int lineSpacing = 2;
    int tooltipHeight = lineHeight * 2 + lineSpacing + padding * 2;
    
    if ((tooltipX + tooltipWidth) * scale > ClickGui.scaledRes.getScaledWidth()) {
      tooltipX -= tooltipWidth + offset * 2;
    }
    
    if ((tooltipY + tooltipHeight) * scale > ClickGui.scaledRes.getScaledHeight()) {
      tooltipY -= tooltipHeight + offset * 2;
    }
    
    final int col = Color.of(50, 50, 50, 255).toBuffer();
    
    SurfaceHelper.drawRect(tooltipX * scale, tooltipY * scale + 1,
      tooltipWidth * scale, tooltipHeight * scale - 2,
      col);
    
    SurfaceHelper.drawRect(tooltipX * scale + 1, tooltipY * scale,
      tooltipWidth * scale - 2, tooltipHeight * scale,
      col);
    
    SurfaceHelper
      .drawTextShadow(modName, (tooltipX + padding) * scale, (tooltipY + padding) * scale,
        0xFFFFFF);
    SurfaceHelper.drawTextShadow(modDescription, (tooltipX + padding) * scale,
      (tooltipY + padding + lineHeight + lineSpacing) * scale, 0xAAAAAA);
  }
  
  public void drawWindow(int mouseX, int mouseY) {
    super.drawWindow(mouseX, mouseY);
    windowY = headerY + 22;
    
    SurfaceHelper.drawOutlinedRectShaded(
      posX, windowY, width, height, Colors.GRAY.toBuffer(), 80, 3);
    int buttonY = windowY - buttonListOffset + 2;
    
    int scale = ClickGui.scaledRes.getScaleFactor();
    
    GL11.glPushMatrix();
    int scissorY = MC.displayHeight - (scale * windowY + scale * height - 3);
    GL11.glScissor(scale * posX, scissorY, scale * width, scale * height - 8);
    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    for (GuiButton button : buttonList) {
      SurfaceHelper.drawRect(posX + 2, buttonY, width - 4, GuiButton.height, button.getColor());
      SurfaceHelper.drawTextShadowCentered(
        button.getName(),
        (posX + 2) + width / 2f,
        buttonY + GuiButton.height / 2f,
        Colors.WHITE.toBuffer());
      button.setCoords(posX + 2, buttonY);
      buttonY += GuiButton.height + 1;
    }
    GL11.glDisable(GL11.GL_SCISSOR_TEST);
    GL11.glPopMatrix();
    
    // update variables
    bottomX = posX + width; // set the coords of the bottom right corner for mouse coord testing
    bottomY = windowY + height;
  }
  
  @Override
  public void drawTooltip(int mouseX, int mouseY) {
    int scale = ClickGui.scaledRes.getScaleFactor();
    
    if (mouseX >= posX && mouseX < bottomX &&
      mouseY >= windowY + (5 / scale) && mouseY < bottomY - (5 / scale)) {
      for (GuiButton button : buttonList) {
        if (mouseX > button.x && mouseX < (button.x + width) &&
          mouseY > button.y && mouseY < (button.y + GuiButton.height)) {
          drawModTooltip(button.getMod(), mouseX, mouseY);
          break;
        }
      }
    }
  }
  
  public void mouseClicked(int x, int y, int state) {
    super.mouseClicked(x, y, state);
    for (GuiButton button : buttonList) {
      if (x > button.x
        && x < (button.x + width)
        && y > button.y
        && y < (button.y + GuiButton.height)
        && !isMouseInHeader(x, y)) {
        button.toggleMod();
        break;
      }
    }
  }
  
  public void handleMouseInput() throws IOException {
    int i = Mouse.getEventDWheel();
    
    i = MathHelper.clamp(i, -1, 1);
    buttonListOffset -= i * 10;
    
    if (buttonListOffset < 0) {
      buttonListOffset = 0; // dont scroll up if its already at the top
    }
    
    int lowestButtonY = (GuiButton.height + 1) * buttonList.size() + windowY;
    int lowestAllowedOffset = lowestButtonY - height - windowY + 3;
    if (lowestButtonY - buttonListOffset < bottomY) {
      buttonListOffset = lowestAllowedOffset;
    }
  }
}
