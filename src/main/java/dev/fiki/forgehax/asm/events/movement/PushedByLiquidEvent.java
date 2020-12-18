package dev.fiki.forgehax.asm.events.movement;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;

@Getter
@AllArgsConstructor
@Cancelable
public class PushedByLiquidEvent extends Event {
  private final PlayerEntity player;
}
