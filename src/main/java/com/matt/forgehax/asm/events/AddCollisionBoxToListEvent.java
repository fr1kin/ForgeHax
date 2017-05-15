package com.matt.forgehax.asm.events;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

/**
 * Created on 4/9/2017 by fr1kin
 */
@Cancelable
public class AddCollisionBoxToListEvent extends Event {
    private final BlockPos pos;
    private final AxisAlignedBB entityBox;
    private final List<AxisAlignedBB> collidingBoxes;
    private final AxisAlignedBB blockBox;

    public AddCollisionBoxToListEvent(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, AxisAlignedBB blockBox) {
        this.pos = pos;
        this.entityBox = entityBox;
        this.collidingBoxes = collidingBoxes;
        this.blockBox = blockBox;
    }

    public BlockPos getPos() {
        return pos;
    }

    public AxisAlignedBB getEntityBox() {
        return entityBox;
    }

    public List<AxisAlignedBB> getCollidingBoxes() {
        return collidingBoxes;
    }

    public AxisAlignedBB getBlockBox() {
        return blockBox;
    }
}
