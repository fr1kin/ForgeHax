package dev.fiki.forgehax.common.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class WaterMovementEvent extends Event {
  private Entity entity;
  private Vec3d movement;
}
