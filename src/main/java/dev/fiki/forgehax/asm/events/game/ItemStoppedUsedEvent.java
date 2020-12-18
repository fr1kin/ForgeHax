package dev.fiki.forgehax.asm.events.game;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;

@Getter
@AllArgsConstructor
@Cancelable
public class ItemStoppedUsedEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity player;
}
