package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerController;
import net.minecraftforge.eventbus.api.Event;

public class PlayerSyncItemEvent extends Event {
  private final PlayerController playerController;

  public PlayerSyncItemEvent(PlayerController playerController) {
    this.playerController = playerController;
  }

  public PlayerController getPlayerController() {
    return playerController;
  }
}
