package com.matt.forgehax.asm.events;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerDamageBlockEvent extends Event {
  private final PlayerControllerMP playerController;
  private final BlockPos pos;
  private final EnumFacing side;

  public PlayerDamageBlockEvent(
      PlayerControllerMP playerController, BlockPos pos, EnumFacing side) {
    this.playerController = playerController;
    this.pos = pos;
    this.side = side;
  }

  public PlayerControllerMP getPlayerController() {
    return playerController;
  }

  public BlockPos getPos() {
    return pos;
  }

  public EnumFacing getSide() {
    return side;
  }
}
