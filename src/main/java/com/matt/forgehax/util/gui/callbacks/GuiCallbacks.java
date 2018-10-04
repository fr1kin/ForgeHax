package com.matt.forgehax.util.gui.callbacks;

import com.matt.forgehax.util.gui.IGuiBase;

/** Created on 9/16/2017 by fr1kin */
public class GuiCallbacks {
  /*
     Wrapper because I don't want to write a addCallbackblahblah for each type
  */

  public static void addButtonClickedCallback(IGuiBase base, IGuiCallbackButtonPressed callback) {
    base.addCallback(IGuiCallbackButtonPressed.class, callback);
  }

  public static void addCheckboxStateChangedCallback(
      IGuiBase base, IGuiCallbackCheckboxStateChanged callback) {
    base.addCallback(IGuiCallbackCheckboxStateChanged.class, callback);
  }

  public static void addChildEventCallback(IGuiBase base, IGuiCallbackChildEvent callback) {
    base.addCallback(IGuiCallbackChildEvent.class, callback);
  }

  public static void addClickedCallback(IGuiBase base, IGuiCallbackClicked callback) {
    base.addCallback(IGuiCallbackClicked.class, callback);
  }

  public static void addFocusChangedCallback(IGuiBase base, IGuiCallbackFocus callback) {
    base.addCallback(IGuiCallbackFocus.class, callback);
  }

  public static void addMouseHoverStateCallback(
      IGuiBase base, IGuiCallbackMouseHoverState callback) {
    base.addCallback(IGuiCallbackMouseHoverState.class, callback);
  }

  public static void addVisibilityChangedCallback(IGuiBase base, IGuiCallbackVisibility callback) {
    base.addCallback(IGuiCallbackVisibility.class, callback);
  }
}
