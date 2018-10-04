package com.matt.forgehax.util.gui;

/** Created on 9/15/2017 by fr1kin */
public interface IGuiWindow extends IGuiParent {
  int getBackgroundColor();

  void setBackgroundColor(int color);

  boolean isCloseable();

  void setCloseable(boolean closeable);

  boolean isCollapsible();

  void setCollapsible(boolean collapsible);

  boolean isCollapsed();

  void setCollapsed(boolean collapsed);

  boolean isDraggable();

  void setDraggable(boolean draggable);
}
