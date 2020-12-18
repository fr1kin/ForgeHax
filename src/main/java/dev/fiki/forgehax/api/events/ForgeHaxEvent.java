package dev.fiki.forgehax.api.events;

import dev.fiki.forgehax.api.event.Event;

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
