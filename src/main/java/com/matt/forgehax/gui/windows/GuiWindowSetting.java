package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiButton;
import com.matt.forgehax.gui.elements.GuiElement;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowSetting extends GuiWindow {

    public ArrayList<GuiElement> inputList = new ArrayList<GuiElement>(); // list of toggles, sliders, text inputs, etc.

    private BaseMod mod;

    public Category category;

    public GuiWindowSetting(Category categoryIn, BaseMod modIn, int x, int y) {
        super(WindowType.SETTING, categoryIn);
        this.mod = modIn;
        category = categoryIn;
        initializeInputs();
    }

    private void initializeInputs() {
        Map<String, Object> settingMap;
    }

    public String getModName() {
        return mod.getModName();
    }

    public BaseMod getMod() {
        return this.mod;
    }

    public void drawWindow(int mouseX, int mouseY) {
        super.drawWindow(mouseX, mouseY);
        windowY = headerY + 22 ;

    }

    public void mouseClicked(int x, int y, int state) {

    }

    public void mouseReleased(int x, int y, int state) {

    }


}
