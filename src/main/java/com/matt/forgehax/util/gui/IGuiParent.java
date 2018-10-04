package com.matt.forgehax.util.gui;

import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import java.util.List;
import javax.annotation.Nullable;

/** Created on 9/9/2017 by fr1kin */
public interface IGuiParent extends IGuiBase {
  void addChild(IGuiBase element);

  void removeChild(IGuiBase element);

  void removeAllChildren();

  List<IGuiBase> getChildren();

  int getChildrenCount();

  boolean focus(IGuiBase element);

  @Nullable
  IGuiBase getChildInFocus();

  void onRenderChildren(GuiRenderEvent event);
}
