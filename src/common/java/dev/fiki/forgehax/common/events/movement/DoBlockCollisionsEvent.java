package dev.fiki.forgehax.common.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class DoBlockCollisionsEvent extends Event {
  private final Entity entity;
  private final BlockPos pos;
  private final BlockState state;
}
