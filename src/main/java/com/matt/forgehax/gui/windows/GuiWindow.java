package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.Category;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;

import static com.matt.forgehax.Globals.MC;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindow {

    public Category category;
    public WindowType type;
    public boolean isHidden; // whether or not not to show everything below the header

    public String title;

    public int posX, headerY, windowY;
    public int bottomX, bottomY;

    // coords of where the window is being dragged from
    public int dragX, dragY;

    private boolean dragging;

    final int maxHeight = (int)(ClickGui.scaledRes.getScaledHeight() * 0.8); // a window can only take up 80% of the height of the window
    public int width = 60, height = maxHeight; // width of the window


    public GuiWindow(WindowType typeIn, Category categoryIn) {
        this.category = categoryIn;
        this.type = typeIn;
        title = categoryIn.getPrettyName();
        if (typeIn.equals(WindowType.SETTING)) title += " Settings";
        ClickGui.windowList.add(this);
    }
    public void setPosition(int x, int y) {
        this.posX = x;
        this.headerY = y;
    }

    public String getTitle() {
        return title;
    }

    public boolean isMouseInHeader(int mouseX, int mouseY) {
        return (mouseX > posX && mouseX < posX+width &&
                mouseY > headerY && mouseY < headerY+20);
    }

    /**
     *  0 == Left Click
     *  1 == Right Click
     *  2 == Middle Click
     */
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
        //scrolling
    }

    public void drawWindow(int mouseX, int mouseY) {
        ClickGui.scaledRes = new ScaledResolution(MC);
        if (dragging) {
            posX = mouseX - dragX;
            headerY = mouseY - dragY;
        }
        drawHeader();
    }

    public void drawHeader() {
        // draw the title of the window
        SurfaceHelper.drawOutlinedRectShaded(posX, headerY, width, 20, Utils.toRGBA(150,150,150,255), 50, 5);
        SurfaceHelper.drawTextShadowCentered(getTitle(), posX+width/2, headerY+10, Utils.toRGBA(255,255,255,255));
    }

    public String getName() {
        return category.getPrettyName();
    }
    public String getDescription() {
        return category.getDescription();
    }
}
