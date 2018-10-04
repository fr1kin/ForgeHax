package com.matt.forgehax.util.gui.mcgui;

import com.matt.forgehax.util.draw.SurfaceBuilder;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.gui.IGuiLabel;
import com.matt.forgehax.util.gui.events.GuiRenderEvent;

/** Created on 9/10/2017 by fr1kin */
public class MLabel extends MBase implements IGuiLabel {
  private String text = "";

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
  public void onUpdateSize() {
    setWidth(SurfaceHelper.getStringWidth(getFontRenderer(), getText()));
    setHeight(SurfaceHelper.getStringHeight(getFontRenderer()));
  }

  @Override
  public void onRenderPreBackground(GuiRenderEvent event) {
    if (isHovered()) {
      event
          .getSurfaceBuilder()
          .push()
          .task(SurfaceBuilder::clearColor)
          .task(SurfaceBuilder::disableTexture2D)
          .task(SurfaceBuilder::enableBlend)
          .color(255, 0, 0, 150)
          .beginQuads()
          .rectangle(getX(), getY(), getWidth(), getHeight())
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
        .text(getText(), getX(), getY())
        .task(SurfaceBuilder::disableFontRendering)
        .task(SurfaceBuilder::disableBlend)
        .pop();
  }
}
