package dev.fiki.forgehax.api.events.render;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Cancelable
@Getter
@AllArgsConstructor
public class NametagRenderEvent extends Event {
  private final Entity entity;
}
