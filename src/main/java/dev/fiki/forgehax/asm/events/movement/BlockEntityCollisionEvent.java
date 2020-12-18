package dev.fiki.forgehax.asm.events.movement;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
@Cancelable
public class BlockEntityCollisionEvent extends Event {
  private final Entity entity;
  private final BlockState blockState;
}
