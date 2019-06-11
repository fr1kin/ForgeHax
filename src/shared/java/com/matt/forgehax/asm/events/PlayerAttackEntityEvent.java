package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class PlayerAttackEntityEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity attacker;
  private final Entity victim;

  public PlayerAttackEntityEvent(
      PlayerController playerController, PlayerEntity attacker, Entity victim) {
    this.playerController = playerController;
    this.attacker = attacker;
    this.victim = victim;
  }

  public PlayerController getPlayerController() {
    return playerController;
  }

  public PlayerEntity getAttacker() {
    return attacker;
  }

  public Entity getVictim() {
    return victim;
  }
}
