package com.matt.forgehax.asm.events.listeners;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface BlockModelRenderListener {
    void onBlockModelRender(IBlockAccess access, IBakedModel model, IBlockState state, BlockPos pos, VertexBuffer buffer);
}
