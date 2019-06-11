package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

public class PlayerDamageBlockEvent extends Event {
  private final PlayerController playerController;
  private final BlockPos pos;
  private final Direction side;

  public PlayerDamageBlockEvent(
      PlayerController playerController, BlockPos pos, Direction side) {
    this.playerController = playerController;
    this.pos = pos;
    this.side = side;
  }

  public PlayerController getPlayerController() {
    return playerController;
  }

  public BlockPos getPos() {
    return pos;
  }

  public Direction getSide() {
    return side;
  }
}
