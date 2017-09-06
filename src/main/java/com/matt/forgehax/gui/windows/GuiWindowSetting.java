package com.matt.forgehax.gui.windows;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.elements.GuiButton;
import com.matt.forgehax.gui.elements.GuiElement;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;

import java.util.ArrayList;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiWindowSetting extends GuiWindow {

    public ArrayList<GuiElement> inputList = new ArrayList<GuiElement>();

    private BaseMod mod;

    public Category category;

    public GuiWindowSetting(Category categoryIn, BaseMod modIn, int x, int y) {
        super(categoryIn);
        this.mod = modIn;
        category = categoryIn;
    }


    public String getModName() {
        return mod.getModName();
    }

    public BaseMod getMod() {
        return this.mod;
    }



}
