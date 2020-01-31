package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.eventbus.api.Event;

@Getter
@Setter
@AllArgsConstructor
public class DrawBlockBoundingBoxEvent extends Event {
  private float red;
  private float green;
  private float blue;
  private float alpha;
  
  public static class Pre extends DrawBlockBoundingBoxEvent {
    public Pre(float r, float g, float b, float a) {
      super(r, g, b, a);
    }
  }
  
  public static class Post extends Event {}
}
