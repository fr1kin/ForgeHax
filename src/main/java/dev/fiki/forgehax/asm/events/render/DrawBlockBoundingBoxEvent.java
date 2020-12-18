package dev.fiki.forgehax.asm.events.render;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class DrawBlockBoundingBoxEvent extends Event {
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Pre extends DrawBlockBoundingBoxEvent {
    private float red;
    private float green;
    private float blue;
    private float alpha;
  }
  
  public static class Post extends DrawBlockBoundingBoxEvent {}
}
