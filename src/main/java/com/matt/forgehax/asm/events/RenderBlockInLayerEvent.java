package com.matt.forgehax.asm.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderBlockInLayerEvent extends Event {
  
  private final Block block;
  private final IBlockState state;
  private final BlockRenderLayer compareToLayer;
  private BlockRenderLayer layer;
  
  public RenderBlockInLayerEvent(
    Block block, IBlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
    this.block = block;
    this.state = state;
    this.layer = layer;
    this.compareToLayer = compareToLayer;
  }
  
  public Block getBlock() {
    return block;
  }
  
  public BlockRenderLayer getLayer() {
    return layer;
  }
  
  public void setLayer(BlockRenderLayer layer) {
    this.layer = layer;
  }
  
  public BlockRenderLayer getCompareToLayer() {
    return compareToLayer;
  }
  
  public IBlockState getState() {
    return state;
  }
}
