package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.matt.forgehax.Globals;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.util.mod.Category;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class ClickGui extends GuiScreen implements Globals {

    public static ClickGui INSTANCE;

    public static ArrayList<GuiWindow> windowList = new ArrayList<GuiWindow>();

    GuiWindowMod combatWindow = new GuiWindowMod(Category.COMBAT);
    GuiWindowMod playerWindow = new GuiWindowMod(Category.PLAYER);
    GuiWindowMod renderWindow = new GuiWindowMod(Category.RENDER);
    GuiWindowMod worldWindow  = new GuiWindowMod(Category.WORLD);
    GuiWindowMod miscWindow   = new GuiWindowMod(Category.MISC);

    public static ScaledResolution scaledRes = new ScaledResolution(MC);

    public int baseColor;

    private ClickGui() {
        // set initial window positions
        //TODO: load from settings
        //TODO: improve this a bit maybe
        for (int i = 0; i < windowList.size(); i++) {
            int x = (i+1) * scaledRes.getScaledWidth()/(windowList.size()+1) - windowList.get(i).width/2-10;
            int y = scaledRes.getScaledHeight()/15;
            windowList.get(i).setPosition(x,y);
        }
    }

    public static ClickGui getInstance() {
        return (INSTANCE == null) ? (INSTANCE = new ClickGui()) : INSTANCE;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    public void moveWindowToTop (GuiWindow window) {
        windowList.remove(window);
        windowList.add(window);
    }

    public boolean isMouseInWindow(int mouseX, int mouseY, GuiWindow window) {
        return mouseX > window.posX  && mouseX < window.bottomX &&
                mouseY > window.headerY && mouseY < window.bottomY;
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (GuiWindow window : windowList) {
            window.drawWindow(mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void mouseClicked(int mouseX, int mouseY, int b) throws IOException {
        try
        {
            for (GuiWindow window : Lists.reverse(windowList)) {
                if (isMouseInWindow(mouseX, mouseY, window)) {
                    window.mouseClicked(mouseX, mouseY, b);
                    moveWindowToTop(window);
                    return;
                }
            }
            super.mouseClicked(mouseX, mouseY, b);
        } catch(Exception e) {}
    }

    public void mouseReleased(int x, int y, int state) {
        for (GuiWindow window : windowList) {
            window.mouseReleased(x, y, state);
        }
        super.mouseReleased(x, y, state);

    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        // will be using this for settings and maybe search
        super.keyTyped(typedChar, keyCode);
    }

    public void handleMouseInput() throws IOException {
        // used for scrolling
        super.handleMouseInput();

        int scale = scaledRes.getScaleFactor();
        for (GuiWindow window : Lists.reverse(windowList)) {
            if (isMouseInWindow(Mouse.getEventX()/scale, (MC.displayHeight-Mouse.getEventY())/scale, window)) {
                window.handleMouseInput();
                break;
            }
        }

    }


}
