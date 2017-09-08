package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiButton;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getModManager;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowMod extends GuiWindow {

    public ArrayList<GuiButton> buttonList = new ArrayList<GuiButton>();
    //public Map<GuiButton, int[]> buttonAreaMap = new HashMap<GuiButton, int[]>(); // map each button to an area of the screen


    int buttonListOffset; // used for scrolling

    public Category category;

    public int listY; // Y value of the modlist - 20 pixels lower than the header Y


    public GuiWindowMod(Category categoryIn) {
        super(WindowType.MODULE, categoryIn);
        category = categoryIn;
        addModsToButtonList();
    }

    private void addModsToButtonList() {
        int index = 0; // index in the buttonList
        int maxWidth = 0;
        int newHeight = 0;
        for(BaseMod mod : getModManager().getMods()) {
            if(mod.getModCategory().equals(category)) {
                GuiButton moduleButton = new GuiButton(mod);
                moduleButton.setIndex(index);
                buttonList.add(moduleButton);
                index++;

                newHeight += moduleButton.height+1;

                String name = moduleButton.getName();
                int width = SurfaceHelper.getTextWidth(name);
                if (width > maxWidth) maxWidth = width;
            }
        }
        height = Math.min(maxHeight, newHeight+3);
        maxWidth = Math.max(60, maxWidth);
        width = maxWidth; // set the width of window to the width of the longest mod name
    }


    public void drawWindow(int mouseX, int mouseY) {
        drawHeader();
        this.listY = headerY + 22 ;
        SurfaceHelper.drawOutlinedRectShaded(posX, listY, width, height, Utils.toRGBA(130,130,130,255), 80, 3);
        int buttonY = listY+2;

        int scale = ClickGui.scaledResolution.getScaleFactor();

        GL11.glPushMatrix();
        int scissorY = MC.displayHeight-(scale*listY+scale*height-4);
        GL11.glScissor(scale*posX, scissorY, scale*width , scale*height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (GuiButton button : buttonList) {
            SurfaceHelper.drawRect(posX+2, buttonY,width-4, 15, Utils.toRGBA(255,0,0, 150));
            buttonY += button.height+1;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();

    }

    public void mouseClicked(int x, int y, int state) {

    }
    public void mouseReleased(int x, int y, int state) {

    }


}
