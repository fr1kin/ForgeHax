package com.matt.forgehax.util;

import static com.matt.forgehax.Helper.getWorld;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockHelper {
  public static BlockInfo newBlockInfo(Block block, int metadata, BlockPos pos) {
    return new BlockInfo(block, metadata, pos);
  }

  public static BlockInfo newBlockInfo(Block block, int metadata) {
    return newBlockInfo(block, metadata, BlockPos.ORIGIN);
  }

  public static BlockInfo newBlockInfo(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    Block block = state.getBlock();
    return newBlockInfo(block, block.getMetaFromState(state), pos);
  }

  public static List<BlockPos> getBlocksInRadius(Vec3d pos, double radius) {
    List<BlockPos> list = Lists.newArrayList();
    for (double x = pos.x - radius; x <= pos.x + radius; ++x) {
      for (double y = pos.y - radius; y <= pos.y + radius; ++y) {
        for (double z = pos.z - radius; z <= pos.z + radius; ++z) {
          list.add(new BlockPos((int) x, (int) y, (int) z));
        }
      }
    }
    return list;
  }

  public static boolean isBlockReplaceable(BlockPos pos) {
    return getWorld().getBlockState(pos).getMaterial().isReplaceable();
  }

  public static class BlockInfo {
    private final Block block;
    private final int metadata;
    private final BlockPos pos;

    private BlockInfo(Block block, int metadata, BlockPos pos) {
      this.block = block;
      this.metadata = metadata;
      this.pos = pos;
    }

    public Block getBlock() {
      return block;
    }

    public int getMetadata() {
      return metadata;
    }

    public BlockPos getPos() {
      return pos;
    }

    public ItemStack asItemStack() {
      return new ItemStack(getBlock(), 1, getMetadata());
    }

    public boolean isInvalid() {
      return Blocks.AIR.equals(getBlock());
    }

    public boolean isEqual(BlockPos pos) {
      IBlockState state = getWorld().getBlockState(pos);
      Block bl = state.getBlock();
      return Objects.equals(getBlock(), bl) && getMetadata() == bl.getMetaFromState(state);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof BlockInfo
              && getBlock().equals(((BlockInfo) obj).getBlock())
              && getMetadata() == ((BlockInfo) obj).getMetadata());
    }

    @Override
    public String toString() {
      return asItemStack().getDisplayName();
    }
  }
}
