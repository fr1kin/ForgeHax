package dev.fiki.forgehax.main.util.events;

import net.minecraftforge.eventbus.api.Event;

public class ForgeHaxEvent extends Event {
  
  public enum Type {
    /**
     * For when eating food
     */
    EATING_SELECT_FOOD,
    EATING_START,
    EATING_STOP,
    
    ;
  }
  
  private final Type type;
  
  public ForgeHaxEvent(Type type) {
    this.type = type;
  }
  
  public Type getType() {
    return type;
  }
}
