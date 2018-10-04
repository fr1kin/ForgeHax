package com.matt.forgehax.util.gui;

/** Created on 9/9/2017 by fr1kin */
public interface IGuiCheckbox extends IGuiBase {
  String getText();

  void setText(String text);

  String getHoverText();

  void setHoverText(String text);

  boolean isChecked();

  void setChecked(boolean checked);

  void onCheckChanged();
}
