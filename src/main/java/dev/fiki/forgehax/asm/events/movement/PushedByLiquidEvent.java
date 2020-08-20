package dev.fiki.forgehax.asm.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class PushedByLiquidEvent extends Event {
  private final PlayerEntity player;
}
