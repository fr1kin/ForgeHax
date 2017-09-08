package com.matt.forgehax.gui.elements;

import com.matt.forgehax.util.mod.BaseMod;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiButton extends GuiElement {

    private BaseMod mod;
    private int index; // top == 0

    public int width;
    public final int height = 15;

    int x, y; // used to get the area they can be clicked in

    public GuiButton(BaseMod modIn) {
        this.mod = modIn;
    }

    public void setIndex(int indexIn) {
        this.index = indexIn;
    }


    public void disableMod() {
        mod.disable();
    }

    public void enableMod() {
        mod.enable();
    }

    public String getName() {
        return mod.getModName();
    }

    public BaseMod getMod() {
        return this.mod;
    }


}
