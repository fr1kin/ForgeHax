package dev.fiki.forgehax.common.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class ApplyClimbableBlockMovement extends Event {
  private final LivingEntity livingEntity;
}
