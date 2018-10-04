package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.gui.GuiHelper;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.callbacks.*;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import javax.annotation.Nullable;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

/** Created on 9/9/2017 by fr1kin */
public class MBase implements IGuiBase {
  protected final CallbackList callbacks = new CallbackList();

  private double x = 0;
  private double y = 0;

  private double width = 0;
  private double height = 0;

  private boolean visible = true;
  private boolean locked = false;

  private IGuiParent parent = null;

  private boolean hovered = false;
  private int hoveredTime = 0;

  private int focusTime = 0;

  private MinecraftFontRenderer fontRenderer = null;
  private Integer fontColor = null;

  @Override
  public void init(double screenWidth, double screenHeight) {
    focusTime = 0;
    hoveredTime = 0;
    hovered = false;
    onUpdateSize();
  }

  @Override
  public double getX() {
    return x;
  }

  @Override
  public double getY() {
    return y;
  }

  @Override
  public void setX(double x) {
    this.x = x;
  }

  @Override
  public void setY(double y) {
    this.y = y;
  }

  @Override
  public double getWidth() {
    return width;
  }

  @Override
  public double getHeight() {
    return height;
  }

  @Override
  public void setWidth(double w) {
    this.width = w;
  }

  @Override
  public void setHeight(double h) {
    this.height = h;
  }

  @Override
  public boolean isVisible() {
    return visible;
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      onVisibleChange();
    }
  }

  @Override
  public boolean isLocked() {
    return locked;
  }

  @Override
  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  @Override
  public IGuiParent getParent() {
    return parent;
  }

  @Override
  public void setParent(IGuiParent parent) {
    if (this.parent == parent) return; // no need to continue
    if (parent == null && this.parent != null) { // remove parent and don't add another
      IGuiParent oldParent = this.parent;
      this.parent = null;
      oldParent.removeChild(this);
    } else {
      if (this.parent != null) { // remove from old parent
        IGuiParent oldParent = this.parent;
        this.parent = null;
        oldParent.removeChild(this);
      }
      this.parent = parent;
      this.parent.addChild(this);
    }
    onUpdateSize();
  }

  @Override
  public boolean isHovered() {
    return hovered;
  }

  @Override
  public int getHoveredTime() {
    return hoveredTime;
  }

  @Override
  public boolean isInFocus() {
    return getParent() == null || getParent().isInFocus() && getParent().getChildInFocus() == this;
  }

  @Override
  public boolean requestFocus() {
    return getParent() != null && getParent().focus(this);
  }

  @Override
  public int getFocusTime() {
    return focusTime;
  }

  @Nullable
  @Override
  public MinecraftFontRenderer getFontRenderer() {
    return fontRenderer != null
        ? fontRenderer
        : (getParent() != null ? getParent().getFontRenderer() : null);
  }

  @Override
  public void setFontRenderer(MinecraftFontRenderer fontRenderer) {
    this.fontRenderer = fontRenderer;
    onUpdateSize();
  }

  @Override
  public int getFontColor() {
    return fontColor != null
        ? fontColor
        : (getParent() != null ? getParent().getFontColor() : Utils.Colors.WHITE);
  }

  @Override
  public void setFontColor(int buffer) {
    this.fontColor = buffer;
  }

  @Override
  public void onUpdateSize() {}

  @Override
  public void onMouseEvent(GuiMouseEvent event) {
    switch (event.getType()) {
      case PRESSED:
        {
          if (event.isMouseWithin(this)) onClicked(event);
          break;
        }
    }
  }

  @Override
  public void onKeyEvent(GuiKeyEvent event) {}

  @Override
  public void onUpdate(GuiUpdateEvent event) {
    // check if mouse is hovered over
    double rx = getRealX();
    double ry = getRealY();
    boolean previous = this.hovered;
    if (GuiHelper.isInArea(
        event.getMouseX(), event.getMouseY(), rx, ry, rx + getWidth(), ry + getHeight())) {
      this.hovered = true;
      this.hoveredTime++;
      if (!previous) onMouseHoverStateChange();
    } else {
      this.hovered = false;
      this.hoveredTime = 0;
      if (previous) onMouseHoverStateChange();
    }

    // update focus time
    if (isInFocus()) this.focusTime++;
    else this.focusTime = 0;
  }

  @Override
  public void onRender(GuiRenderEvent event) {
    onRenderPreBackground(event);

    onRenderPostBackground(event);
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {}

  @Override
  public void onRenderPostBackground(GuiRenderEvent event) {}

  @Override
  public <T extends IGuiCallbackBase> void addCallback(Class<T> clazz, T callback) {
    callbacks.add(clazz, callback);
  }

  @Override
  public <T extends IGuiCallbackBase> void removeCallback(Class<T> clazz, T callback) {
    callbacks.remove(clazz, callback);
  }

  @Override
  public void onVisibleChange() {
    if (!isVisible()) {
      focusTime = 0;
      hoveredTime = 0;
      hovered = false;
    }
    callbacks
        .get(IGuiCallbackVisibility.class)
        .forEach(IGuiCallbackVisibility::onVisibilityChanged);
  }

  @Override
  public void onFocusChanged() {
    callbacks.get(IGuiCallbackFocus.class).forEach(IGuiCallbackFocus::onFocusChanged);
  }

  @Override
  public void onMouseHoverStateChange() {
    callbacks
        .get(IGuiCallbackMouseHoverState.class)
        .forEach(IGuiCallbackMouseHoverState::onMouseHoverStateChange);
  }

  @Override
  public void onClicked(final GuiMouseEvent event) {
    callbacks.get(IGuiCallbackClicked.class).forEach(listener -> listener.onClicked(event));
  }
}
