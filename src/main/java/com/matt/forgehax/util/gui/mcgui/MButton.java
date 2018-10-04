package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiButton;
import com.matt.forgehax.util.gui.callbacks.IGuiCallbackButtonPressed;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/** Created on 9/16/2017 by fr1kin */
public class MButton extends MBase implements IGuiButton {
  private boolean pressed = true;

  private String text = "";
  private String hoverText = "";

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
  public boolean isPressed() {
    return pressed;
  }

  @Override
  public void setPressed(boolean pressed) {
    if (pressed != this.pressed) {
      this.pressed = pressed;
      onPressed();
    }
  }

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
  public void onPressed() {
    callbacks.get(IGuiCallbackButtonPressed.class).forEach(IGuiCallbackButtonPressed::onPressed);
  }

  @Override
  public void onClicked(GuiMouseEvent event) {
    if (event.isLeftMouse()) setPressed(!isPressed());
    super.onClicked(event);
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {
    event
        .getSurfaceBuilder()
        .task(SurfaceBuilder::clearColor)
        .task(SurfaceBuilder::disableTexture2D)
        .task(SurfaceBuilder::enableBlend)
        .push()
        .translate(getX(), getY())
        .color(isPressed() ? Utils.Colors.GREEN : getBackgroundColor())
        .beginQuads()
        .rectangle(0, 0, getWidth(), getHeight())
        .end()
        .color(Utils.toRGBA(0, 0, 0, 255))
        .beginLineLoop()
        .rectangle(0, 0, getWidth(), getHeight())
        .end()
        .pop()
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend);

    // GL11.glScissor((int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
    // GL11.glEnable(GL11.GL_SCISSOR_TEST);

    double strWidth = SurfaceHelper.getStringWidth(getFontRenderer(), getText());
    double strHeight = SurfaceHelper.getStringHeight(getFontRenderer());

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
            getX() + (getWidth() / 2) - (strWidth / 2),
            getY() + (getHeight() / 2) - (strHeight / 2))
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend)
        .pop();

    // GL11.glDisable(GL11.GL_SCISSOR_TEST);
  }
}
