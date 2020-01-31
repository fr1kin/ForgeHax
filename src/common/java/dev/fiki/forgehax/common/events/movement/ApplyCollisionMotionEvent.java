package dev.fiki.forgehax.common.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

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
