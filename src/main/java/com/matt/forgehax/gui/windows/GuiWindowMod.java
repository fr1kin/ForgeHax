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
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getModManager;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowMod extends GuiWindow {

    public ArrayList<GuiButton> buttonList = new ArrayList<GuiButton>();

    /**
     * The button list y coord needs to be offset to move them up or down the window
     * 0 = natural state
     * anything above 0 means the button list has moved up and the user has scrolled down
     */
    private int buttonListOffset;

    public Category category;

    public int listY; // Y value of the modlist - 20 pixels lower than the header Y


    public GuiWindowMod(Category categoryIn) {
        super(WindowType.MODULE, categoryIn);
        category = categoryIn;
        addModsToButtonList();
    }

    private void addModsToButtonList() {
        int maxWidth = 0;
        int newHeight = 0;
        for(BaseMod mod : getModManager().getMods()) {
            if(mod.getModCategory().equals(category) && !mod.isHidden()) {
                GuiButton moduleButton = new GuiButton(mod);
                buttonList.add(moduleButton);

                newHeight += moduleButton.height+1;

                String name = moduleButton.getName();
                int width = SurfaceHelper.getTextWidth(name);
                if (width > maxWidth) maxWidth = width;
            }
        }
        height = Math.min(maxHeight, newHeight+3);
        width = maxWidth+15; // set the width of window to the width of the longest mod name

    }


    public void drawWindow(int mouseX, int mouseY) {
        ClickGui.scaledRes = new ScaledResolution(MC);
        super.drawWindow(mouseX, mouseY);
        this.listY = headerY + 22 ;

        SurfaceHelper.drawOutlinedRectShaded(posX, listY, width, height, Utils.toRGBA(130,130,130,255), 80, 3);
        int buttonY = listY - buttonListOffset + 2;

        int scale = ClickGui.scaledRes.getScaleFactor();

        GL11.glPushMatrix();
        int scissorY = MC.displayHeight-(scale*listY+scale*height-3);
        GL11.glScissor(scale*posX, scissorY, scale*width , scale*height-8);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (GuiButton button : buttonList) {
            SurfaceHelper.drawRect(posX+2, buttonY,width-4, 15, button.getColor());
            SurfaceHelper.drawTextShadowCentered(button.getName(),(posX+2)+width/2, buttonY+button.height/2, Utils.toRGBA(255,255,255,255));
            button.setCoords(posX +2, buttonY);
            buttonY += button.height+1;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();

        //update variables
        bottomX = posX + width; // set the coords of the bottom right corner for mouse coord testing
        bottomY = listY + height;
    }

    public void mouseClicked(int x, int y, int state) {
        super.mouseClicked(x, y, state);
        for (GuiButton button : buttonList) {
            if (x > button.x && x < (button.x + width) &&
                    y > button.y && y < (button.y + button.height))
            {
                if (button.isModEnabled())
                    button.disableMod();
                else
                    button.enableMod();
                return;
            }
        }
    }

    public void mouseReleased(int x, int y, int state) {
        super.mouseReleased(x,y,state);
    }


    public void handleMouseInput() throws IOException {
        int i = Mouse.getEventDWheel();

        i = MathHelper.clamp(i, -1, 1);
        buttonListOffset -= i*5;

        if (buttonListOffset < 0) buttonListOffset = 0; // dont scroll up if its already at the top
        int lowestButtonY = 16 * buttonList.size() + listY;
        int lowestAllowedOffset = lowestButtonY - height - listY + 3;
        if (lowestButtonY - buttonListOffset < bottomY)
            buttonListOffset = lowestAllowedOffset;

    }


}
