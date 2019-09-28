package com.matt.forgehax.asm.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 11/10/2016 by fr1kin
 */
public class BlockRenderEvent extends Event {
  
  private final BlockPos pos;
  private final IBlockState state;
  private final IBlockAccess access;
  private final BufferBuilder buffer;
  
  public BlockRenderEvent(
    BlockPos pos, IBlockState state, IBlockAccess access, BufferBuilder buffer) {
    this.pos = pos;
    this.state = state;
    this.access = access;
    this.buffer = buffer;
  }
  
  public BlockPos getPos() {
    return pos;
  }
  
  public IBlockAccess getAccess() {
    return access;
  }
  
  public IBlockState getState() {
    return state;
  }
  
  public BufferBuilder getBuffer() {
    return buffer;
  }
}
