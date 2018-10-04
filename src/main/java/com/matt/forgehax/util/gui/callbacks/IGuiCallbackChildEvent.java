package com.matt.forgehax.util.gui.callbacks;

import com.matt.forgehax.util.gui.IGuiBase;

/** Created on 9/16/2017 by fr1kin */
public interface IGuiCallbackChildEvent extends IGuiCallbackBase {
  void onChildAdded(IGuiBase child);

  void onChildRemoved(IGuiBase child);
}
