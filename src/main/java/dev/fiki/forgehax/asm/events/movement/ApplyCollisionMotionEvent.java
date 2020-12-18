package dev.fiki.forgehax.asm.events.movement;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
@Cancelable
public class ApplyCollisionMotionEvent extends Event {
  private Entity entity;
  private Entity collidedWithEntity;
  private double motionX;
  private double motionY;
  private double motionZ;
}
