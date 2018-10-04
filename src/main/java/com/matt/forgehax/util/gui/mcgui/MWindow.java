package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.gui.GuiHelper;
import com.matt.forgehax.util.gui.IGuiWindow;
import com.matt.forgehax.util.gui.callbacks.GuiCallbacks;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/** Created on 9/15/2017 by fr1kin */
public class MWindow extends MParent implements IGuiWindow {
  public static final double BAR_HEIGHT = 20;
  public static final double BUTTON_SIZE = 7;
  public static final double BUFFER = 2;

  private final MButton closeButton = new MButton();
  private final MButton collapseButton = new MButton();

  private boolean closeable = true;
  private boolean collapsible = true;
  private boolean draggable = true;

  private boolean dragging = false;

  private double dragX = 0.D;
  private double dragY = 0.D;

  private int backgroundColor = Utils.Colors.WHITE;

  public MWindow() {
    closeButton.setText("X");
    closeButton.setFontColor(Utils.Colors.BLACK);
    closeButton.setParent(this);
    closeButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
    closeButton.setVisible(isCloseable());

    collapseButton.setText("-");
    collapseButton.setFontColor(Utils.Colors.BLACK);
    collapseButton.setParent(this);
    collapseButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
    collapseButton.setVisible(isCollapsible());
    GuiCallbacks.addButtonClickedCallback(
        collapseButton,
        () -> {
          collapseButton.setText(isCollapsed() ? "+" : "-");
          children
              .stream()
              .filter(gui -> gui != closeButton && gui != collapseButton)
              .forEach(gui -> gui.setVisible(collapseButton.isPressed()));
        });

    repositionButtons();
  }

  private void repositionButtons() {
    if (isCloseable()) {
      closeButton.setX(getWidth() - closeButton.getWidth() - BUFFER);
      closeButton.setY(BAR_HEIGHT / 2 - closeButton.getHeight() / 2);
    }

    if (isCollapsible()) {
      collapseButton.setX(
          (isCloseable() ? closeButton.getX() : getWidth()) - collapseButton.getWidth() - BUFFER);
      collapseButton.setY(BAR_HEIGHT / 2 - collapseButton.getHeight() / 2);
    }
  }

  @Override
  public int getBackgroundColor() {
    return backgroundColor;
  }

  @Override
  public void setBackgroundColor(int color) {
    this.backgroundColor = color;
  }

  @Override
  public boolean isCloseable() {
    return closeable;
  }

  @Override
  public void setCloseable(boolean closeable) {
    if (closeable != this.closeable) {
      this.closeable = closeable;
      repositionButtons();
    }
  }

  @Override
  public boolean isCollapsible() {
    return collapsible;
  }

  @Override
  public void setCollapsible(boolean collapsible) {
    if (collapsible != this.collapsible) {
      this.collapsible = collapsible;
      repositionButtons();
    }
  }

  @Override
  public boolean isCollapsed() {
    return !collapseButton.isPressed();
  }

  @Override
  public void setCollapsed(boolean collapsed) {
    collapseButton.setPressed(collapsed);
  }

  @Override
  public boolean isDraggable() {
    return draggable;
  }

  @Override
  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  @Override
  public void onClicked(GuiMouseEvent event) {
    requestFocus();
    super.onClicked(event);
  }

  @Override
  public double getHeight() {
    return isCollapsed() ? BAR_HEIGHT : super.getHeight();
  }

  @Override
  public void setHeight(double h) {
    super.setHeight(Math.max(h, BAR_HEIGHT));
  }

  @Override
  public boolean isVisible() {
    return closeButton.isPressed();
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible != closeButton.isPressed()) {
      closeButton.setPressed(visible);
      onVisibleChange();
    }
  }

  @Override
  public void onFocusChanged() {
    if (!isInFocus()) {
      // focus lost
      dragging = false;
    }
    super.onFocusChanged();
  }

  @Override
  public void onUpdateSize() {
    repositionButtons();

    super.onUpdateSize();
  }

  @Override
  public void onMouseEvent(GuiMouseEvent event) {
    super.onMouseEvent(event);

    if (event.isLeftMouse()
        && GuiHelper.isInRectangle(
            event.getMouseX(), event.getMouseY(), getRealX(), getRealY(), getWidth(), BAR_HEIGHT)) {
      switch (event.getType()) {
        case PRESSED:
          dragging = true;
        case DOWN:
          {
            dragX = event.getMouseX() - getRealX();
            dragY = event.getMouseY() - getRealY();
            break;
          }
        case RELEASED:
          dragging = false;
          break;
      }
    }
  }

  @Override
  public void onRender(GuiRenderEvent event) {
    if (isDraggable() && dragging) {
      setX(event.getMouseX() - dragX);
      setY(event.getMouseY() - dragY);
    }

    super.onRender(event);
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {
    super.onRenderPreBackground(event);

    event
        .getSurfaceBuilder()
        .task(SurfaceBuilder::clearColor)
        .task(SurfaceBuilder::disableTexture2D)
        .task(SurfaceBuilder::enableBlend);

    // bar
    event
        .getSurfaceBuilder()
        .push()
        .color(getBackgroundColor())
        .beginQuads()
        .rectangle(getX(), getY(), getWidth(), BAR_HEIGHT)
        .end()
        .color(0, 0, 0, 255)
        .beginLineLoop()
        .rectangle(getX(), getY(), getWidth(), BAR_HEIGHT)
        .end()
        .pop();

    // bar headings
    if (isCloseable() || isCollapsible()) {}

    // actual window
    if (getHeight() > BAR_HEIGHT && !isCollapsed()) {
      event
          .getSurfaceBuilder()
          .push()
          .color(getBackgroundColor())
          .beginQuads()
          .rectangle(getX(), getY() + BAR_HEIGHT, getWidth(), getHeight() - BAR_HEIGHT)
          .end()
          .color(0, 0, 0, 255)
          .beginLineLoop()
          .rectangle(getX(), getY() + BAR_HEIGHT, getWidth(), getHeight() - BAR_HEIGHT)
          .end()
          .pop();
    }

    event
        .getSurfaceBuilder()
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend);
  }
}
