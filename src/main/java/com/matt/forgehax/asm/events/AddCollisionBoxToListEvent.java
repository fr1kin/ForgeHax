package com.matt.forgehax.asm.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

/**
 * Created on 4/9/2017 by fr1kin
 */
public class AddCollisionBoxToListEvent extends Event {
    private final Block block;
    private final IBlockState state;
    private final World world;
    private final List<AxisAlignedBB> alignedBB;
    private final BlockPos pos;

    public AddCollisionBoxToListEvent(Block block, IBlockState state, World world, List<AxisAlignedBB> axis, BlockPos pos) {
        this.block = block;
        this.state = state;
        this.world = world;
        this.alignedBB = axis;
        this.pos = pos;
    }

    public Block getBlock() {
        return block;
    }

    public IBlockState getState() {
        return state;
    }

    public World getWorld() {
        return world;
    }

    public List<AxisAlignedBB> getAlignedBB() {
        return alignedBB;
    }

    public BlockPos getPos() {
        return pos;
    }
}
