package dev.fiki.forgehax.api.events.render;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Cancelable
public class FogDensityRenderEvent extends Event {
  private float density;
}
