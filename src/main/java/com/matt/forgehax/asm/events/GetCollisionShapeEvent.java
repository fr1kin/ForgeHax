package com.matt.forgehax.asm.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.eventbus.api.Event;

public class GetCollisionShapeEvent extends Event {
    public final Block block;
    public final IBlockState state;
    public final IBlockReader world;
    public final BlockPos pos;

    private VoxelShape returnShape;

    public GetCollisionShapeEvent(Block block, IBlockState state, IBlockReader world, BlockPos pos) {
        this.block = block;
        this.state = state;
        this.world = world;
        this.pos = pos;
    }

    public void setReturnShape(VoxelShape newShape) {
        this.returnShape = newShape;
    }

    public VoxelShape getReturnShape() {
        return returnShape;
    }
}
