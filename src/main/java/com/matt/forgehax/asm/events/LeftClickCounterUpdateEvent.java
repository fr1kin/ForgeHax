package com.matt.forgehax.asm.events;

import com.matt.forgehax.asm.reflection.FastReflection;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LeftClickCounterUpdateEvent extends Event {
  
  private final Minecraft minecraft;
  private int value;

  public LeftClickCounterUpdateEvent(Minecraft minecraft, int value) {
    this.minecraft = minecraft;
    this.value = value;
  }

  public Minecraft getMinecraft() {
    return minecraft;
  }

  public int getCurrentValue() {
    return FastReflection.Fields.Minecraft_leftClickCounter.get(minecraft);
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
