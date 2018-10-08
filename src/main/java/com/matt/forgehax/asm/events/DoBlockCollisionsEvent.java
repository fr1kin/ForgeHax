package com.matt.forgehax.asm.events;

import com.matt.forgehax.asm.events.abstractevents.EntityEvent;
import com.matt.forgehax.util.event.Cancelable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class DoBlockCollisionsEvent extends EntityEvent implements Cancelable {
    private final BlockPos pos;
    private final IBlockState state;

    public DoBlockCollisionsEvent(Entity entity, BlockPos pos, IBlockState state) {
        super(entity);
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }
}
