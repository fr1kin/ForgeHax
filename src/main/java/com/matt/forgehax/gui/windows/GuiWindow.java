package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.Category;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindow {

    Category category;
    boolean isHidden; // whether or not not to show everything below the header

    private int x, y;

    public GuiWindow(Category categoryIn) {
        category = categoryIn;
        ClickGui.windowList.add(this);
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void mouseClicked(int x, int y, int state) {

    }
    public void mouseReleased(int x, int y, int state) {

    }

    public void drawWindow() {
        drawHeader();
    }

    private void drawHeader() {
        // draw the title of the window
        SurfaceHelper.drawOutlinedRectShaded(x, y, 80, 30, Utils.toRGBA(150,150,150,255), 50, 5);
    }

    public String getName() {
        return category.getPrettyName();
    }
    public String getDescription() {
        return category.getDescription();
    }
}
