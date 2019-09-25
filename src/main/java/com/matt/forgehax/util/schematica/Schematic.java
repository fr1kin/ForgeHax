package com.matt.forgehax.util.schematica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public interface Schematic {
  default IBlockState desiredState(int x, int y, int z) {
    return desiredState(new BlockPos(x, y, z));
  }

  IBlockState desiredState(BlockPos pos);

  default boolean inSchematic(BlockPos pos) {
    return inSchematic(pos.getX(), pos.getY(), pos.getZ());
  }

  default boolean inSchematic(int x, int y, int z) {
    return x >= 0 && x < widthX() && y >= 0 && y < heightY() && z >= 0 && z < lengthZ();
  }

  int widthX();

  int heightY();

  int lengthZ();
}
