package com.matt.forgehax.util.gui.callbacks;

import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;

/**
 * Created on 9/15/2017 by fr1kin
 */
public class GuiCallback {
    // BASE
    public void onVisibleChange() {}

    public void onFocusChange() {}
    public void onMouseHoverChange() {}

    public void onClicked(GuiMouseEvent event) {}

    // PARENT
    public void onChildAdded(IGuiBase child) {}
    public void onChildRemoved(IGuiBase child) {}

    // CHECKBOX
    public void onCheckChange() {}
}
