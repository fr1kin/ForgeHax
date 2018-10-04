package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.gui.IGuiPanel;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/** Created on 9/9/2017 by fr1kin */
public class MPanel extends MParent implements IGuiPanel {
  private boolean collapsed = false;

  private int maxRows = Integer.MAX_VALUE;
  private int maxColumns = 1;

  private boolean verticalScrolling = false;
  private boolean horizontalScrolling = false;

  private int backgroundColor = Utils.Colors.WHITE;

  @Override
  public int getBackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void setBackgroundColor(int color) {
    this.backgroundColor = color;
  }

  @Override
  public boolean isCollapsed() {
    return collapsed;
  }

  @Override
  public void setCollapsed(boolean collapsed) {
    this.collapsed = true;
  }

  @Override
  public int getMaxRows() {
    return maxRows;
  }

  @Override
  public void setMaxRows(int rows) {
    this.maxRows = Math.max(1, rows);
  }

  @Override
  public int getMaxColumns() {
    return maxColumns;
  }

  @Override
  public void setMaxColumns(int columns) {
    this.maxColumns = Math.max(1, columns);
  }

  @Override
  public boolean isVerticalScrolling() {
    return verticalScrolling;
  }

  @Override
  public void setVerticalScrolling(boolean scrolling) {
    this.verticalScrolling = scrolling;
  }

  @Override
  public boolean isHorizontalScrolling() {
    return horizontalScrolling;
  }

  @Override
  public void setHorizontalScrolling(boolean scrolling) {
    this.horizontalScrolling = scrolling;
  }

  @Override
  public void onClicked(GuiMouseEvent event) {
    requestFocus(); // request focus on click

    super.onClicked(event);
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {
    super.onRenderPreBackground(event);

    event
        .getSurfaceBuilder()
        .task(SurfaceBuilder::clearColor)
        .task(SurfaceBuilder::disableTexture2D)
        .task(SurfaceBuilder::enableBlend)
        .push()
        .color(getBackgroundColor())
        .beginQuads()
        .rectangle(getX(), getY(), getWidth(), getHeight())
        .end()
        .color(0, 0, 0, 255)
        .beginLineLoop()
        .rectangle(getX(), getY(), getWidth(), getHeight())
        .end()
        .pop()
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend);

    /*
           .line(getX(), getY(), getX(), getY() + getHeight())
           .line(getX(), getY() + getHeight(), getX() + getWidth(), getY() + getHeight())
           .line(getX() + getWidth(), getY() + getHeight(), getX() + getWidth(), getY())
           .line(getX() + getWidth(), getY(), getX(), getY())
    */
  }
}
