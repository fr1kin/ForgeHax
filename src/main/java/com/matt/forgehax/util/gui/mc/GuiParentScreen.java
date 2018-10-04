package com.matt.forgehax.util.gui.mc;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.gui.mcgui.MParent;

/** Created on 9/15/2017 by fr1kin */
public class GuiParentScreen extends MParent implements Globals {
  @Override
  public void init(double screenWidth, double screenHeight) {
    setSize(screenWidth, screenHeight);
    super.init(screenWidth, screenHeight);
  }
}
