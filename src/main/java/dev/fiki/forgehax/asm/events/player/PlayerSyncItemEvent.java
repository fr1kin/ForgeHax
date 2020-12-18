package dev.fiki.forgehax.asm.events.player;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;

@Getter
@AllArgsConstructor
public class PlayerSyncItemEvent extends Event {
  private final PlayerController playerController;
}
