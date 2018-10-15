package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerAttackEntityEvent extends Event {
  private final PlayerControllerMP playerController;
  private final EntityPlayer attacker;
  private final Entity victim;

  public PlayerAttackEntityEvent(
      PlayerControllerMP playerController, EntityPlayer attacker, Entity victim) {
    this.playerController = playerController;
    this.attacker = attacker;
    this.victim = victim;
  }

  public PlayerControllerMP getPlayerController() {
    return playerController;
  }

  public EntityPlayer getAttacker() {
    return attacker;
  }

  public Entity getVictim() {
    return victim;
  }
}
