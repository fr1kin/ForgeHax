package com.github.lunatrius.schematica.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface ISchematic {

    BlockState getBlockState(BlockPos var1);

    int getWidth();

    int getHeight();

    int getLength();
}
