package com.matt.forgehax.gui;

import com.matt.forgehax.Globals;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.util.mod.Category;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class ClickGui extends GuiScreen implements Globals {

    public static ClickGui clickGui;

    public static ArrayList<GuiWindow> windowList = new ArrayList<GuiWindow>();

    GuiWindowMod combatWindow = new GuiWindowMod(Category.COMBAT);
    GuiWindowMod playerWindow = new GuiWindowMod(Category.PLAYER);
    GuiWindowMod renderWindow = new GuiWindowMod(Category.RENDER);
    GuiWindowMod worldWindow  = new GuiWindowMod(Category.WORLD);
    GuiWindowMod miscWindow   = new GuiWindowMod(Category.MISC);

    public static ScaledResolution scaledResolution = new ScaledResolution(MC);


    public void initGui(){
        // set initial window positions
        //TODO: load from settings
        //TODO: improve this a bit maybe
        for (int i = 0; i < windowList.size(); i++) {
            int x = (i+1) * scaledResolution.getScaledWidth()/(windowList.size()+1) - windowList.get(i).width/2-10;
            int y = scaledResolution.getScaledHeight()/15;
            windowList.get(i).setPosition(x,y);
        }
    }

    public static ClickGui getClickGui() {
        return (ClickGui.clickGui == null) ? (ClickGui.clickGui = new ClickGui()) : ClickGui.clickGui;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    public void moveWindowToTop (GuiWindow window) {
        windowList.remove(window);
        windowList.add(window);
    }


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (GuiWindow window : windowList) {
            window.drawWindow(mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void mouseClicked(int x, int y, int b) throws IOException { // TODO: check which one is on top
        try
        {
            for (GuiWindow window : windowList) {
                window.mouseClicked(x, y, b);
            }
            super.mouseClicked(x, y, b);
        }catch(Exception e) {}
    }

    public void mouseReleased(int x, int y, int state) {
        for (GuiWindow window : windowList) {
            window.mouseReleased(x, y, state);
        }
        super.mouseReleased(x, y, state);

    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        // will be using this for settings and maybe search
        if (keyCode == 1)
        {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
    }



}
