package com.matt.forgehax.util.gui.callbacks;

import com.matt.forgehax.util.gui.events.GuiMouseEvent;

/** Created on 9/16/2017 by fr1kin */
public interface IGuiCallbackClicked extends IGuiCallbackBase {
  void onClicked(GuiMouseEvent event);
}
