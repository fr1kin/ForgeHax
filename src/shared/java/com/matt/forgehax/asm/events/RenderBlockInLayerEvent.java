package com.matt.forgehax.asm.events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.eventbus.api.Event;

public class RenderBlockInLayerEvent extends Event {
  private final Block block;
  private final BlockState state;
  private final BlockRenderLayer compareToLayer;
  private BlockRenderLayer layer;

  public RenderBlockInLayerEvent(
      Block block, BlockState state, BlockRenderLayer layer, BlockRenderLayer compareToLayer) {
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

  public BlockState getState() {
    return state;
  }
}
