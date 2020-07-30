package dev.fiki.forgehax.asm.events.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 6/15/2017 by fr1kin
 */
@Getter
@AllArgsConstructor
@Cancelable
public class PostPlayerMovementUpdateEvent extends Event {
  private final ClientPlayerEntity localPlayer;
}
