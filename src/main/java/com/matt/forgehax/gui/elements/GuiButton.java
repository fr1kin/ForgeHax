package com.matt.forgehax.gui.elements;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.BaseMod;

/**
 * Created by Babbaj on 9/5/2017.
 */
public class GuiButton extends GuiElement {

    private BaseMod mod;

    public final int colorEnabled = Utils.toRGBA(80,80,80, 200);
    public final int colorDisabled = Utils.toRGBA(100,100,100,150);


    public int width;
    public final int height = 15;

    public int x, y; // used to get the area they can be clicked in

    public GuiButton(BaseMod modIn) {
        this.mod = modIn;
    }


    public void setCoords(int xIn, int yIn) {
        this.x = xIn;
        this.y = yIn;
    }

    public boolean isModEnabled() {
        return mod.isEnabled();
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

    public int getColor() {
        return isModEnabled() ? colorEnabled : colorDisabled;
    }


}
