package com.matt.forgehax.asm.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.Event;

public class BlockControllerProcessEvent extends Event {
  
  private final Minecraft minecraft;
  private boolean leftClicked;

  public BlockControllerProcessEvent(Minecraft minecraft, boolean leftClicked) {
    this.minecraft = minecraft;
    this.leftClicked = leftClicked;
  }

  public Minecraft getMinecraft() {
    return minecraft;
  }

  public boolean isLeftClicked() {
    return leftClicked;
  }

  public void setLeftClicked(boolean clicked) {
    this.leftClicked = clicked;
  }
}
