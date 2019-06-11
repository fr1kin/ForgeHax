package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ItemStoppedUsedEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity player;

  public ItemStoppedUsedEvent(PlayerController playerController, PlayerEntity player) {
    this.playerController = playerController;
    this.player = player;
  }

  public PlayerController getPlayerController() {
    return playerController;
  }

  public PlayerEntity getPlayer() {
    return player;
  }
}
