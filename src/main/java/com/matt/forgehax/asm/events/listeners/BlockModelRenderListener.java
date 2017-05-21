package com.matt.forgehax.asm.events.listeners;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Set;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface BlockModelRenderListener {
    void onBlockRenderInLoop(RenderChunk renderChunk, Block block, IBlockState state, BlockPos pos);
}
