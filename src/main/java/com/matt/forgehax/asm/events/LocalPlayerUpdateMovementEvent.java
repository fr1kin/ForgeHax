package com.matt.forgehax.asm.events;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 6/15/2017 by fr1kin
 */
public class LocalPlayerUpdateMovementEvent extends Event {
  
  private final EntityPlayerSP localPlayer;

  private LocalPlayerUpdateMovementEvent(EntityPlayerSP localPlayer) {
    this.localPlayer = localPlayer;
  }

  public EntityPlayerSP getLocalPlayer() {
    return localPlayer;
  }

  @Cancelable
  public static class Pre extends LocalPlayerUpdateMovementEvent {
  
    public Pre(EntityPlayerSP localPlayer) {
      super(localPlayer);
    }
  }

  public static class Post extends LocalPlayerUpdateMovementEvent {
  
    public Post(EntityPlayerSP localPlayer) {
      super(localPlayer);
    }
  }
}
