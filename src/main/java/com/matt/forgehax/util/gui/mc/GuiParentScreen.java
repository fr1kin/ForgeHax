package com.matt.forgehax.util.gui.mc;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.gui.mcgui.MParent;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Created on 9/15/2017 by fr1kin
 */
public class GuiParentScreen extends MParent implements Globals {
    public GuiParentScreen() {
        ScaledResolution res = new ScaledResolution(MC);
        setSize(res.getScaledWidth_double(), res.getScaledHeight_double());
    }
}
