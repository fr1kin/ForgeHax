package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.callbacks.IGuiCallbackBase;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import javax.annotation.Nullable;
import uk.co.hexeption.thx.ttf.MinecraftFontRenderer;

/** Created on 9/17/2017 by fr1kin */
public class ImmutableGuiBase implements IGuiBase {
  @Override
  public void init(double screenWidth, double screenHeight) {}

  @Override
  public double getX() {
    return 0;
  }

  @Override
  public double getY() {
    return 0;
  }

  @Override
  public void setX(double x) {}

  @Override
  public void setY(double y) {}

  @Override
  public double getWidth() {
    return 0;
  }

  @Override
  public double getHeight() {
    return 0;
  }

  @Override
  public void setWidth(double w) {}

  @Override
  public void setHeight(double h) {}

  @Override
  public boolean isVisible() {
    return false;
  }

  @Override
  public void setVisible(boolean visible) {}

  @Override
  public boolean isLocked() {
    return true;
  }

  @Override
  public void setLocked(boolean locked) {}

  @Nullable
  @Override
  public IGuiParent getParent() {
    return null;
  }

  @Override
  public void setParent(@Nullable IGuiParent parent) {}

  @Override
  public boolean isHovered() {
    return false;
  }

  @Override
  public int getHoveredTime() {
    return 0;
  }

  @Override
  public boolean isInFocus() {
    return false;
  }

  @Override
  public boolean requestFocus() {
    return false;
  }

  @Override
  public int getFocusTime() {
    return 0;
  }

  @Nullable
  @Override
  public MinecraftFontRenderer getFontRenderer() {
    return null;
  }

  @Override
  public void setFontRenderer(MinecraftFontRenderer fontRenderer) {}

  @Override
  public int getFontColor() {
    return 0;
  }

  @Override
  public void setFontColor(int buffer) {}

  @Override
  public void onUpdateSize() {}

  @Override
  public void onMouseEvent(GuiMouseEvent event) {}

  @Override
  public void onKeyEvent(GuiKeyEvent event) {}

  @Override
  public void onUpdate(GuiUpdateEvent event) {}

  @Override
  public void onRender(GuiRenderEvent event) {}

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {}

  @Override
  public void onRenderPostBackground(GuiRenderEvent event) {}

  @Override
  public <T extends IGuiCallbackBase> void addCallback(Class<T> clazz, T callback) {}

  @Override
  public <T extends IGuiCallbackBase> void removeCallback(Class<T> clazz, T callback) {}

  @Override
  public void onVisibleChange() {}

  @Override
  public void onFocusChanged() {}

  @Override
  public void onMouseHoverStateChange() {}

  @Override
  public void onClicked(GuiMouseEvent event) {}
}
