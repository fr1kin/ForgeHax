package dev.fiki.forgehax.asm.events.movement;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;

/**
 * Created on 6/15/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
@Cancelable
public class PostMovementUpdateEvent extends Event {
  private final ClientPlayerEntity localPlayer;
}
