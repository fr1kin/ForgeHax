package com.matt.forgehax.util.gui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.gui.callbacks.IGuiCallbackBase;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import javax.annotation.Nullable;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

/** Created on 9/9/2017 by fr1kin */
public interface IGuiBase {
  /**
   * Initialize GUI. Called when created or when the UI is rescaled.
   *
   * @param screenWidth scaled width
   * @param screenHeight scaled height
   */
  void init(double screenWidth, double screenHeight);

  /**
   * The X coordinate of this element. If the element has a parent, then this will be relative to
   * that parent (i.e if a Panel is at (5, 5) and the pos of this element is (1, 1), the REAL
   * position is (6, 6) and the relative position is (1,1).
   *
   * @return relative X coordinate
   */
  double getX();

  /**
   * The Y coordinate of this element. If the element has a parent, then this will be relative to
   * that parent (i.e if a Panel is at (5, 5) and the pos of this element is (1, 1), the REAL
   * position is (6, 6) and the relative position is (1,1).
   *
   * @return relative Y coordinate
   */
  double getY();

  /**
   * Sets the relative X coordinate
   *
   * @param x relative X coordinate
   */
  void setX(double x);

  /**
   * Sets the relative Y coordinate
   *
   * @param y relative Y coordinate
   */
  void setY(double y);

  /**
   * Sets both the X and Y coordinates at the same time. By default, calls setX(D) and setY(D).
   *
   * @param x relative X coordinate
   * @param y relative Y coordinate
   */
  default void setPos(double x, double y) {
    setX(x);
    setY(y);
  }

  /**
   * Get the real X coordinate
   *
   * @return X coordinate relative to the screen
   */
  default double getRealX() {
    return getParent() == null ? getX() : getParent().getRealX() + getX();
  }

  /**
   * Get the real Y coordinate
   *
   * @return Y coordinate relative to the screen
   */
  default double getRealY() {
    return getParent() == null ? getY() : getParent().getRealY() + getY();
  }

  /**
   * Width of the element
   *
   * @return width
   */
  double getWidth();

  /**
   * Height of the element
   *
   * @return height
   */
  double getHeight();

  /**
   * Set the width of the element
   *
   * @param w width
   */
  void setWidth(double w);

  /**
   * Set the height of the element
   *
   * @param h height
   */
  void setHeight(double h);

  /**
   * Set the width and height at the same time By default, calls setWidth(D) and setHeight(D)
   *
   * @param w width
   * @param h height
   */
  default void setSize(double w, double h) {
    setWidth(w);
    setHeight(h);
  }

  /**
   * Check if the element is visible. If not visible, element will not update or draw.
   *
   * @return if the element will draw
   */
  boolean isVisible();

  /**
   * Enable/disable element rendering and updating
   *
   * @param visible true to enable, false to disable
   */
  void setVisible(boolean visible);

  boolean isLocked();

  void setLocked(boolean locked);

  /**
   * This elements parent
   *
   * @return null if no parent exists
   */
  @Nullable
  IGuiParent getParent();

  /**
   * Set this elements parent. If it already has a parent, then the child will leave that parent and
   * join the provided one.
   *
   * @param parent null to remove its parent
   */
  void setParent(@Nullable IGuiParent parent);

  /**
   * If the mouse is hovering over this element
   *
   * @return true if mouse is hovering over
   */
  boolean isHovered();

  /**
   * Time that the mouse has been hovering over the element.
   *
   * @return time in render ticks
   */
  int getHoveredTime();

  /**
   * If this element has focus
   *
   * @return true if has focus
   */
  boolean isInFocus();

  /**
   * Will give this element top focus NOTE: Two of the same elements are not allowed sequentially,
   * but are however allowed if split up. If this method is called and the element is already at the
   * top of the stack, nothing will be added.
   */
  boolean requestFocus();

  /**
   * Time this element has had top focus
   *
   * @return time in render ticks
   */
  int getFocusTime();

  @Nullable
  MinecraftFontRenderer getFontRenderer();

  void setFontRenderer(MinecraftFontRenderer fontRenderer);

  int getFontColor();

  void setFontColor(int buffer);

  default void setFontColor(int r, int g, int b, int a) {
    setFontColor(Utils.toRGBA(r, g, b, a));
  }

  void onUpdateSize();

  /**
   * Called when a mouse event is invoked
   *
   * @param event event data
   */
  void onMouseEvent(GuiMouseEvent event);

  /**
   * Called when a keyboard event is invoked
   *
   * @param event event data
   */
  void onKeyEvent(GuiKeyEvent event);

  /**
   * Called before rendering
   *
   * @param event event data
   */
  void onUpdate(GuiUpdateEvent event);

  void onRender(GuiRenderEvent event);

  void onRenderPreBackground(GuiRenderEvent event);

  void onRenderPostBackground(GuiRenderEvent event);

  <T extends IGuiCallbackBase> void addCallback(Class<T> clazz, T callback);

  <T extends IGuiCallbackBase> void removeCallback(Class<T> clazz, T callback);

  void onVisibleChange();

  void onFocusChanged();

  void onMouseHoverStateChange();

  void onClicked(GuiMouseEvent event);
}
