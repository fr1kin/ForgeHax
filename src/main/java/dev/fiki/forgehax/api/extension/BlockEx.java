package dev.fiki.forgehax.api.extension;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockEx {
  public static boolean isPlaceable(Block block, World world, BlockPos pos) {
    return block.defaultBlockState().isCollisionShapeFullBlock(world, pos);
  }
}
