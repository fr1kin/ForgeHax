package com.matt.forgehax.asm.events;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderTabNameEvent extends Event {
  
  private final String name;
  private final int color;
  
  private @Nullable
  String newName;
  private OptionalInt newColor = OptionalInt.empty();
  
  public RenderTabNameEvent(String name, int color) {
    this.name = name;
    this.color = color;
  }
  
  public void setName(String newName) {
    this.newName = newName;
  }
  
  public void setColor(int newColor) {
    this.newColor = OptionalInt.of(newColor);
  }
  
  public String getName() {
    return this.newName != null ? this.newName : this.name;
  }
  
  public int getColor() {
    return newColor.orElse(this.color);
  }
}
