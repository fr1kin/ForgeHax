package com.matt.forgehax.util.gui;

import java.util.Optional;

/** Created on 9/14/2017 by fr1kin */
public class GuiHelper {
  public static boolean isInArea(
      double x, double y, double topX, double topY, double bottomX, double bottomY) {
    return x > topX && x < bottomX && y > topY && y < bottomY;
  }

  public static boolean isInRectangle(
      double x, double y, double topX, double topY, double width, double height) {
    return isInArea(x, y, topX, topY, topX + width, topY + height);
  }

  public static Optional<IGuiBase> getTopGuiAt(IGuiParent parent, final double x, final double y) {
    return parent
        .getChildren()
        .stream()
        .filter(IGuiBase::isVisible)
        .filter(
            gui ->
                isInRectangle(
                    x, y, gui.getRealX(), gui.getRealY(), gui.getWidth(), gui.getHeight()))
        .findFirst();
  }
}
