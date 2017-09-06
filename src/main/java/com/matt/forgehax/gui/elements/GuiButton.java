package com.matt.forgehax.gui.elements;

import com.matt.forgehax.util.mod.BaseMod;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiButton extends GuiElement {

    private BaseMod mod;
    int position; // top = 0

    public GuiButton(BaseMod modIn) {
        this.mod = modIn;
    }

    public void disable() {
        mod.disable();
    }

    public void enable() {
        mod.enable();
    }

    public String getName() {
        return mod.getModName();
    }

    public BaseMod getMod() {
        return this.mod;
    }


}
