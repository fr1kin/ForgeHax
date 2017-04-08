package com.matt.forgehax.asm2;

import com.matt.forgehax.asm2.events.CanBlockRenderInLayerEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created on 1/28/2017 by fr1kin
 */
public class MethodOverrides {
    //
    // Block
    //
    public static boolean canRenderInLayerOverride(String originalMethodName, Block block, IBlockState state, BlockRenderLayer compareToLayer) {
        //OriginalMethodCall originalMethodCall = OriginalMethods.get(originalMethodName);
        //return originalMethodCall.invoke(Block.class, block, new Class[] {IBlockState.class, BlockRenderLayer.class}, new Object[]{state, compareToLayer});
        CanBlockRenderInLayerEvent event = new CanBlockRenderInLayerEvent(block, state, compareToLayer);
        MinecraftForge.EVENT_BUS.post(event);
        return event.canRenderInLayer();
    }
}
