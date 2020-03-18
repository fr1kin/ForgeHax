package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class BlockEntityCollisionEvent extends Event {
  private final Entity entity;
  private final BlockState blockState;
}
