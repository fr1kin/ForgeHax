package com.matt.forgehax.asm.events;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.eventbus.api.Event;

/** Created on 11/10/2016 by fr1kin */
// TODOL make sure IWorldReader is correct
public class BlockRenderEvent extends Event {
  private final BlockPos pos;
  private final BlockState state;
  private final IWorldReader access;
  private final BufferBuilder buffer;

  public BlockRenderEvent(
      BlockPos pos, BlockState state, IWorldReader access, BufferBuilder buffer) {
    this.pos = pos;
    this.state = state;
    this.access = access;
    this.buffer = buffer;
  }

  public BlockPos getPos() {
    return pos;
  }

  public IWorldReader getAccess() {
    return access;
  }

  public BlockState getState() {
    return state;
  }

  public BufferBuilder getBuffer() {
    return buffer;
  }
}
