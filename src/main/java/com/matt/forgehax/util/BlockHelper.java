package com.matt.forgehax.util;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.util.entity.LocalPlayerUtils.getServerViewAngles;
import static com.matt.forgehax.util.entity.LocalPlayerUtils.isInReach;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.math.VectorUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class BlockHelper {
  public static UniqueBlock newUniqueBlock(Block block, int metadata, BlockPos pos) {
    return new UniqueBlock(block, metadata, pos);
  }

  public static UniqueBlock newUniqueBlock(Block block, int metadata) {
    return newUniqueBlock(block, metadata, BlockPos.ORIGIN);
  }

  public static UniqueBlock newUniqueBlock(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    Block block = state.getBlock();
    return newUniqueBlock(block, block.getMetaFromState(state), pos);
  }

  public static BlockTraceInfo newBlockTrace(BlockPos pos, EnumFacing side) {
    return new BlockTraceInfo(pos, side);
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

  public static boolean isTraceClear(Vec3d start, Vec3d end, EnumFacing targetSide) {
    RayTraceResult tr = getWorld().rayTraceBlocks(start, end, false, true, false);
    return tr == null || (new BlockPos(end).equals(new BlockPos(tr.hitVec)) && targetSide.getOpposite().equals(tr.sideHit));
  }

  public static Vec3d getOBBCenter(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    AxisAlignedBB bb = state.getBoundingBox(getWorld(), pos);
    return new Vec3d(
        bb.minX + ((bb.maxX - bb.minX) / 2.D),
        bb.minY + ((bb.maxY - bb.minY) / 2.D),
        bb.minZ + ((bb.maxZ - bb.minZ) / 2.D));
  }

  public static boolean isBlockPlaceable(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    return state.getBlock().canCollideCheck(state, false);
  }

  private static BlockTraceInfo getPlaceableBlockSide(Stream<EnumFacing> stream, final BlockPos pos) {
    final Vec3d eyes = EntityUtils.getEyePos(getLocalPlayer());
    final Vec3d normal = getServerViewAngles().getDirectionVector().normalize();
    return stream
        .map(side -> newBlockTrace(pos.offset(side), side))
        .filter(info -> isBlockPlaceable(info.getPos()))
        .filter(info -> isInReach(eyes, info.getHitVec()))
        .filter(info -> BlockHelper.isTraceClear(eyes, info.getHitVec(), info.getSide()))
        .min(
            Comparator.comparingDouble(
                info -> VectorUtils.getCrosshairDistance(eyes, normal, info.getCenteredPos())))
        .orElse(null);
  }
  public static BlockTraceInfo getPlaceableBlockSide(EnumSet<EnumFacing> sides, final BlockPos pos) {
    return getPlaceableBlockSide(sides.stream(), pos);
  }
  public static BlockTraceInfo getPlaceableBlockSide(final BlockPos pos) {
    return getPlaceableBlockSide(Stream.of(EnumFacing.values()), pos);
  }

  public static class BlockTraceInfo {
    private final BlockPos pos;
    private final EnumFacing side;
    private final Vec3d center;
    private final Vec3d hitVec;

    private BlockTraceInfo(BlockPos pos, EnumFacing side) {
      this.pos = pos;
      this.side = side;
      Vec3d obb = BlockHelper.getOBBCenter(pos);
      this.center = new Vec3d(pos).add(obb);
      this.hitVec =
          this.center.add(
              VectorUtils.multiplyBy(new Vec3d(getOppositeSide().getDirectionVec()), obb));
    }

    public BlockPos getPos() {
      return pos;
    }

    public EnumFacing getSide() {
      return side;
    }

    public EnumFacing getOppositeSide() {
      return side.getOpposite();
    }

    public Vec3d getHitVec() {
      return this.hitVec;
    }

    public Vec3d getCenteredPos() {
      return center;
    }

    public IBlockState getBlockState() {
      return getWorld().getBlockState(getPos());
    }
  }

  public static class UniqueBlock {
    private final Block block;
    private final int metadata;
    private final BlockPos pos;

    private UniqueBlock(Block block, int metadata, BlockPos pos) {
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

    public Vec3d getCenteredPos() {
      return new Vec3d(getPos()).add(getOBBCenter(getPos()));
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
          || (obj instanceof UniqueBlock
              && getBlock().equals(((UniqueBlock) obj).getBlock())
              && getMetadata() == ((UniqueBlock) obj).getMetadata());
    }

    @Override
    public String toString() {
      return getBlock().getRegistryName().toString() + "{" + getMetadata() + "}";
    }
  }
}
