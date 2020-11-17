package dev.fiki.forgehax.api.schematica;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface Schematic {
  
  default BlockState desiredState(int x, int y, int z) {
    return desiredState(new BlockPos(x, y, z));
  }

  BlockState desiredState(BlockPos pos);
  
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
