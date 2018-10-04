package com.matt.forgehax.util.gui.mcgui;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.gui.GuiHelper;
import com.matt.forgehax.util.gui.IGuiBase;
import com.matt.forgehax.util.gui.IGuiParent;
import com.matt.forgehax.util.gui.callbacks.IGuiCallbackChildEvent;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;
import com.matt.forgehax.util.gui.events.GuiUpdateEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.GlStateManager;

/** Created on 9/15/2017 by fr1kin */
public class MParent extends MBase implements IGuiParent {
  protected final List<IGuiBase> children = Lists.newCopyOnWriteArrayList();

  @Override
  public void addChild(IGuiBase element) {
    if (!children.contains(element) && children.add(element)) {
      element.setParent(this);
      focus(element); // to invoke focus callbacks and add to head
      callbacks
          .get(IGuiCallbackChildEvent.class)
          .forEach(listener -> listener.onChildAdded(element));
    }
  }

  @Override
  public void removeChild(IGuiBase element) {
    if (children.remove(element)) {
      element.setParent(null);
      callbacks
          .get(IGuiCallbackChildEvent.class)
          .forEach(listener -> listener.onChildRemoved(element));
    }
  }

  @Override
  public void removeAllChildren() {
    children.forEach(this::removeChild);
  }

  @Override
  public List<IGuiBase> getChildren() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public int getChildrenCount() {
    return children.size();
  }

  @Override
  public boolean focus(IGuiBase element) {
    if (this == element.getParent() && element != getChildInFocus()) {
      Optional<IGuiBase> previous =
          Optional.ofNullable(getChildInFocus()).filter(gui -> element != gui);

      children.remove(element); // remove from list
      children.add(0, element); // readd at head of the stack

      previous.ifPresent(IGuiBase::onFocusChanged);

      element.onFocusChanged();

      return true;
    } else return false;
  }

  @Nullable
  @Override
  public IGuiBase getChildInFocus() {
    return children.isEmpty() ? null : children.get(0);
  }

  @Override
  public void onRenderChildren(GuiRenderEvent event) {
    for (int i = children.size() - 1;
        i >= 0;
        --i) { // since rendering last = top, we must iterate the list backwards for rendering
      IGuiBase gui = children.get(i);
      if (gui.isVisible()) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(getX(), getY(), 0.D);

        gui.onRender(event);

        GlStateManager.popMatrix();
      }
    }
  }

  @Override
  public void init(double screenWidth, double screenHeight) {
    super.init(screenWidth, screenHeight);

    for (IGuiBase gui : children)
      if (gui.isVisible()) {
        gui.init(screenWidth, screenHeight);
      }
  }

  @Override
  public void onUpdateSize() {
    super.onUpdateSize();
    for (IGuiBase gui : children)
      if (gui.isVisible()) {
        gui.onUpdateSize();
      }
  }

  @Override
  public void onMouseEvent(GuiMouseEvent event) {
    Optional<IGuiBase> gui = GuiHelper.getTopGuiAt(this, event.getMouseX(), event.getMouseY());
    if (gui.isPresent()) gui.get().onMouseEvent(event);
    else super.onMouseEvent(event);
  }

  @Override
  public void onKeyEvent(GuiKeyEvent event) {
    super.onKeyEvent(event);

    // TODO: there will always be a child on top, need a find a way to give focus only to 1 at a
    // time
    Optional.ofNullable(getChildInFocus())
        .filter(IGuiBase::isVisible)
        .ifPresent(gui -> gui.onKeyEvent(event));
  }

  @Override
  public void onUpdate(GuiUpdateEvent event) {
    super.onUpdate(event);

    // update children if visible
    for (IGuiBase gui : children) if (gui.isVisible()) gui.onUpdate(event);
  }

  @Override
  public void onRender(GuiRenderEvent event) {
    // don't call super

    onRenderPreBackground(event);

    onRenderChildren(event);

    onRenderPostBackground(event);
  }
}
