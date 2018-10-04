package com.matt.forgehax.util.gui;

/** Created on 9/16/2017 by fr1kin */
public interface IGuiButton {
  int getBackgroundColor();

  void setBackgroundColor(int color);

  boolean isPressed();

  void setPressed(boolean pressed);

  String getText();

  void setText(String text);

  String getHoverText();

  void setHoverText(String text);

  void onPressed();
}
