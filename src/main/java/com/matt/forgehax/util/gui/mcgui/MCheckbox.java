package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiCheckbox;
import com.matt.forgehax.util.gui.callbacks.IGuiCallbackCheckboxStateChanged;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/** Created on 9/9/2017 by fr1kin */
public class MCheckbox extends MBase implements IGuiCheckbox {
  public static final int CHECKBOX_SIZE = 10;
  public static final int BUFFER_SIZE = 2;

  private String text = "";
  private String hoverText = "";

  private boolean checked = false;

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void setText(String text) {
    this.text = text;
    onUpdateSize();
  }

  @Override
  public String getHoverText() {
    return hoverText;
  }

  @Override
  public void setHoverText(String text) {
    this.hoverText = text;
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void setChecked(boolean checked) {
    if (checked != this.checked) {
      this.checked = checked;
      onCheckChanged();
    }
  }

  @Override
  public void onCheckChanged() {
    callbacks
        .get(IGuiCallbackCheckboxStateChanged.class)
        .forEach(IGuiCallbackCheckboxStateChanged::onCheckChanged);
  }

  @Override
  public void onUpdateSize() {
    super.onUpdateSize();
    setWidth(
        SurfaceHelper.getStringWidth(getFontRenderer(), getText()) + BUFFER_SIZE + CHECKBOX_SIZE);
    setHeight(Math.max(CHECKBOX_SIZE, SurfaceHelper.getStringHeight(getFontRenderer())));
  }

  @Override
  public void onClicked(GuiMouseEvent event) {
    if (event.isLeftMouse()) setChecked(!isChecked());
    super.onClicked(event);
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {
    double fontHeight = SurfaceHelper.getStringHeight(getFontRenderer());

    // draw check mark box
    event
        .getSurfaceBuilder()
        .push()
        .task(SurfaceBuilder::clearColor)
        .task(SurfaceBuilder::disableTexture2D)
        .task(SurfaceBuilder::enableBlend)
        .translate(getX(), getY())
        .color(Utils.Colors.BLACK)
        .beginQuads()
        .rectangle(0, 0, CHECKBOX_SIZE, CHECKBOX_SIZE)
        .end()
        .color(Utils.Colors.WHITE)
        .beginQuads()
        .rectangle(1, 1, CHECKBOX_SIZE - 2, CHECKBOX_SIZE - 2)
        .end()
        .task(SurfaceBuilder::enableTexture2D)
        .task(SurfaceBuilder::disableBlend)
        .pop();

    if (isChecked()) {
      event
          .getSurfaceBuilder()
          .push()
          .task(SurfaceBuilder::clearColor)
          .task(SurfaceBuilder::disableTexture2D)
          .task(SurfaceBuilder::enableBlend)
          .translate(getX(), getY())
          .color(Utils.Colors.GREEN)
          .beginQuads()
          .rectangle(2, 2, CHECKBOX_SIZE - 4, CHECKBOX_SIZE - 4)
          .end()
          .task(SurfaceBuilder::enableTexture2D)
          .task(SurfaceBuilder::disableBlend)
          .pop();
    }

    event
        .getSurfaceBuilder()
        .push()
        .task(SurfaceBuilder::enableTexture2D)
        .task(SurfaceBuilder::enableFontRendering)
        .task(SurfaceBuilder::enableBlend)
        .fontRenderer(getFontRenderer())
        .color(getFontColor())
        .text(
            getText(),
            getX() + CHECKBOX_SIZE + BUFFER_SIZE,
            getY() - (fontHeight / 2) + (CHECKBOX_SIZE / 2))
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend)
        .pop();

    if (isHovered() && getHoveredTime() > 10) {
      double rx = getParent() == null ? 0.D : getParent().getRealX();
      double ry = getParent() == null ? 0.D : getParent().getRealY();

      event
          .getSurfaceBuilder()
          .push()
          .translate(-rx + event.getMouseX() + 10, -ry + event.getMouseY() - fontHeight)
          .task(SurfaceBuilder::clearColor)
          .task(SurfaceBuilder::disableTexture2D)
          .task(SurfaceBuilder::enableBlend)
          .color(Utils.Colors.BLACK)
          .beginQuads()
          .rectangle(
              -2,
              -2,
              SurfaceHelper.getStringWidth(getFontRenderer(), getHoverText()) + 4,
              fontHeight + 4)
          .end()
          .task(SurfaceBuilder::enableTexture2D)
          .task(SurfaceBuilder::enableFontRendering)
          .fontRenderer(getFontRenderer())
          .color(getFontColor())
          .text(getHoverText(), 0, 0)
          .task(SurfaceBuilder::disableFontRendering)
          .task(SurfaceBuilder::disableBlend)
          .pop();
    }
  }
}
