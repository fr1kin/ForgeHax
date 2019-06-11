package com.matt.forgehax.asm.events;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Created on 6/15/2017 by fr1kin */
public class LocalPlayerUpdateMovementEvent extends Event {
  private final ClientPlayerEntity localPlayer;

  private LocalPlayerUpdateMovementEvent(ClientPlayerEntity localPlayer) {
    this.localPlayer = localPlayer;
  }

  public ClientPlayerEntity getLocalPlayer() {
    return localPlayer;
  }

  @Cancelable
  public static class Pre extends LocalPlayerUpdateMovementEvent {
    public Pre(ClientPlayerEntity localPlayer) {
      super(localPlayer);
    }
  }

  public static class Post extends LocalPlayerUpdateMovementEvent {
    public Post(ClientPlayerEntity localPlayer) {
      super(localPlayer);
    }
  }
}
