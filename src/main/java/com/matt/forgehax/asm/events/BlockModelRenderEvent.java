package com.matt.forgehax.asm.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class BlockModelRenderEvent extends Event {
  
  private final IBlockAccess blockAccess;
  private final IBakedModel bakedModel;
  private final IBlockState blockState;
  private final BlockPos blockPos;
  private final BufferBuilder buffer;
  private final boolean checkSides;
  private final long rand;
  
  public BlockModelRenderEvent(
    IBlockAccess worldIn,
    IBakedModel modelIn,
    IBlockState stateIn,
    BlockPos posIn,
    BufferBuilder buffer,
    boolean checkSides,
    long rand) {
    this.blockAccess = worldIn;
    this.bakedModel = modelIn;
    this.blockState = stateIn;
    this.blockPos = posIn;
    this.buffer = buffer;
    this.checkSides = checkSides;
    this.rand = rand;
  }
  
  public IBlockAccess getBlockAccess() {
    return blockAccess;
  }
  
  public IBakedModel getBakedModel() {
    return bakedModel;
  }
  
  public IBlockState getBlockState() {
    return blockState;
  }
  
  public BlockPos getBlockPos() {
    return blockPos;
  }
  
  public BufferBuilder getBuffer() {
    return buffer;
  }
  
  public boolean isCheckSides() {
    return checkSides;
  }
  
  public long getRand() {
    return rand;
  }
}
