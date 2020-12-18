package dev.fiki.forgehax.api.events.entity;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;

@Getter
@RequiredArgsConstructor
public class LocalPlayerUpdateEvent extends Event {
  private final ClientPlayerEntity player;
}
