package com.matt.forgehax.util.gui.events;

import com.matt.forgehax.util.draw.SurfaceBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

/** Created on 9/10/2017 by fr1kin */
public class GuiRenderEvent {
  private final SurfaceBuilder surfaceBuilder = new SurfaceBuilder();

  private final Tessellator tessellator;
  private final BufferBuilder builder;

  private final float partialTicks;

  private final int mouseX;
  private final int mouseY;

  public GuiRenderEvent(Tessellator tessellator, float partialTicks, int mouseX, int mouseY) {
    this.tessellator = tessellator;
    this.builder = tessellator.getBuffer();
    this.partialTicks = partialTicks;
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }

  public SurfaceBuilder getSurfaceBuilder() {
    return surfaceBuilder;
  }

  public Tessellator getTessellator() {
    return tessellator;
  }

  public BufferBuilder getBuilder() {
    return builder;
  }

  public float getPartialTicks() {
    return partialTicks;
  }

  public int getMouseX() {
    return mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }
}
