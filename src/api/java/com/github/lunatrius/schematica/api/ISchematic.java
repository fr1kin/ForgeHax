package com.github.lunatrius.schematica.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface ISchematic {

    IBlockState getBlockState(BlockPos var1);

    int getWidth();

    int getHeight();

    int getLength();
}
