package dev.fiki.forgehax.asm.events.movement;


import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;

@Cancelable
@AllArgsConstructor
public class PushedByBlockEvent extends Event {
  private final ClientPlayerEntity player;
}
