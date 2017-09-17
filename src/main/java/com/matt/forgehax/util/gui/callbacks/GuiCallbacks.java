package com.matt.forgehax.util.gui.callbacks;

import com.matt.forgehax.util.gui.IGuiBase;

/**
 * Created on 9/16/2017 by fr1kin
 */
public class GuiCallbacks {
    /*
        Wrapper because I don't want to write a addCallbackblahblah for each type
     */

    public static void newButtonPressed(IGuiBase base, IGuiCallbackButtonPressed callback) {
        base.addCallback(IGuiCallbackButtonPressed.class, callback);
    }

    public static void newCheckboxStateChanged(IGuiBase base, IGuiCallbackCheckboxStateChanged callback) {
        base.addCallback(IGuiCallbackCheckboxStateChanged.class, callback);
    }

    public static void newChildEvent(IGuiBase base, IGuiCallbackChildEvent callback) {
        base.addCallback(IGuiCallbackChildEvent.class, callback);
    }

    public static void newClicked(IGuiBase base, IGuiCallbackClicked callback) {
        base.addCallback(IGuiCallbackClicked.class, callback);
    }

    public static void newFocusChanged(IGuiBase base, IGuiCallbackFocus callback) {
        base.addCallback(IGuiCallbackFocus.class, callback);
    }

    public static void newMouseHoverState(IGuiBase base, IGuiCallbackMouseHoverState callback) {
        base.addCallback(IGuiCallbackMouseHoverState.class, callback);
    }

    public static void newVisibilityChanged(IGuiBase base, IGuiCallbackVisibility callback) {
        base.addCallback(IGuiCallbackVisibility.class, callback);
    }
}
