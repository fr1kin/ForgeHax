package com.matt.forgehax.gui;

import com.matt.forgehax.Globals;
import com.matt.forgehax.gui.windows.GuiWindow;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.util.mod.Category;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.Sys;

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

    ScaledResolution scaledResolution = new ScaledResolution(MC);

    public void initGui() {
        // set initial window positions
        //TODO: load from settings
        for (int i = 0; i <= windowList.size()-1; i++) {
            int x = (i+1) * scaledResolution.getScaledWidth()/(windowList.size()+1);
            int y = scaledResolution.getScaledHeight()/10;
            windowList.get(i).setPosition(x,y);
            System.out.print("ClickGui: " + x + " " + y);
        }
        System.out.println("Set initial window positions");
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


    public void drawScreen(int x, int y, float partialTicks) {
        for (GuiWindow window : windowList) {
            window.drawWindow();
        }
        super.drawScreen(x, y, partialTicks);
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
            this.mc.displayGuiScreen((GuiScreen)null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
    }



}
