package dev.fiki.forgehax.asm.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class PlayerSyncItemEvent extends Event {
  private final PlayerController playerController;
}
