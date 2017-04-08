package com.matt.forgehax.asm2.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 1/29/2017 by fr1kin
 */
public class CanBlockRenderInLayerEvent extends Event {
    private final Block block;
    private final IBlockState state;
    private BlockRenderLayer compareToBlockRenderLayer;

    public CanBlockRenderInLayerEvent(Block block, IBlockState state, BlockRenderLayer compareToBlockRenderLayer) {
        this.block = block;
        this.state = state;
        this.compareToBlockRenderLayer = compareToBlockRenderLayer;
    }

    public Block getBlock() {
        return block;
    }

    public IBlockState getState() {
        return state;
    }

    public BlockRenderLayer getCompareToBlockRenderLayer() {
        return compareToBlockRenderLayer;
    }

    public void setCompareToBlockRenderLayer(BlockRenderLayer compareToBlockRenderLayer) {
        this.compareToBlockRenderLayer = compareToBlockRenderLayer;
    }

    public boolean canRenderInLayer() {
        return block.getBlockLayer().equals(compareToBlockRenderLayer);
    }
}
