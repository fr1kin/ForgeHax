package com.github.lunatrius.core.util.math;

import net.minecraft.util.math.BlockPos;

public class MBlockPos extends BlockPos {

    MBlockPos() {
        super(6, 6, 6);
    }

    @Override
    public int getX() {
        throw new LinkageError("LOL");
    }

    @Override
    public int getY() {
        throw new LinkageError("LOL");
    }

    @Override
    public int getZ() {
        throw new LinkageError("LOL");
    }
}
