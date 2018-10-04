package com.matt.forgehax.util.gui.events;

/** Created on 9/10/2017 by fr1kin */
public class GuiUpdateEvent {
  private final int mouseX;
  private final int mouseY;

  public GuiUpdateEvent(int mouseX, int mouseY) {
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }

  public int getMouseX() {
    return mouseX;
  }

  public int getMouseY() {
    return mouseY;
  }
}
