package dev.fiki.forgehax.asm.events.render;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NearClippingPlaneEvent extends Event {
  public float value;
}
