package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ItemStoppedUsedEvent extends Event {
  
  private final PlayerControllerMP playerController;
  private final EntityPlayer player;
  
  public ItemStoppedUsedEvent(PlayerControllerMP playerController, EntityPlayer player) {
    this.playerController = playerController;
    this.player = player;
  }
  
  public PlayerControllerMP getPlayerController() {
    return playerController;
  }
  
  public EntityPlayer getPlayer() {
    return player;
  }
}
