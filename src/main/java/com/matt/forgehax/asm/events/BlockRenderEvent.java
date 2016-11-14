package com.matt.forgehax.asm.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Created on 11/10/2016 by fr1kin
 */
@Cancelable
public class BlockRenderEvent extends BlockEvent {
    public BlockRenderEvent(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
    }
}
