package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import net.minecraftforge.eventbus.api.Event;

@AllArgsConstructor
public class NearClippingPlaneEvent extends Event {
  public float value;
}
