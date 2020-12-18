package dev.fiki.forgehax.api.events.entity;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;

@Getter
@AllArgsConstructor
public class LivingUpdateEvent extends Event {
  private final LivingEntity living;
}
