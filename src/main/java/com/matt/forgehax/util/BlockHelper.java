package com.matt.forgehax.util;

import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.asm.reflection.FastReflection.Methods.Block_onBlockActivated;
import static com.matt.forgehax.util.entity.LocalPlayerUtils.isInReach;

import com.google.common.collect.Lists;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.utils.ReflectionHelper;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.VectorUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import org.apache.commons.lang3.NotImplementedException;

public class BlockHelper {
  public static UniqueBlock newUniqueBlock(IBlockState blockState, BlockPos pos) {
    return new UniqueBlock(blockState, pos);
  }

  public static UniqueBlock newUniqueBlock(IBlockState blockState) {
    return newUniqueBlock(blockState, BlockPos.ORIGIN);
  }

  public static UniqueBlock newUniqueBlock(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    return newUniqueBlock(state, pos);
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

  public static float getBlockHardness(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    return state.getBlockHardness(getWorld(), pos);
  }

  public static boolean isBlockReplaceable(BlockPos pos) {
    return getWorld().getBlockState(pos).getMaterial().isReplaceable();
  }

  public static boolean isTraceClear(Vec3d start, Vec3d end, EnumFacing targetSide) {
    RayTraceResult tr = getWorld().rayTraceBlocks(start, end, RayTraceFluidMode.NEVER, true, false);
    return tr == null
        || (new BlockPos(end).equals(new BlockPos(tr.hitVec))
            && targetSide.getOpposite().equals(tr.sideHit));
  }

  public static Vec3d getOBBCenter(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    // TODO: make sure we get the right shape
    AxisAlignedBB bb = state.getShape(Helper.getWorld(), pos).getBoundingBox();
    //AxisAlignedBB bb = state.getBoundingBox(getWorld(), pos);
    return new Vec3d(
        bb.minX + ((bb.maxX - bb.minX) / 2.D),
        bb.minY + ((bb.maxY - bb.minY) / 2.D),
        bb.minZ + ((bb.maxZ - bb.minZ) / 2.D));
  }

  public static boolean isBlockPlaceable(BlockPos pos) {
    IBlockState state = getWorld().getBlockState(pos);
    return state.getBlock().isCollidable(state);
  }

  private static BlockTraceInfo getPlaceableBlockSideTrace(
      Vec3d eyes, Vec3d normal, Stream<EnumFacing> stream, BlockPos pos) {
    return stream
        .map(side -> newBlockTrace(pos.offset(side), side))
        .filter(info -> isBlockPlaceable(info.getPos()))
        .filter(info -> isInReach(eyes, info.getHitVec()))
        .filter(info -> BlockHelper.isTraceClear(eyes, info.getHitVec(), info.getSide()))
        .min(
            Comparator.<BlockTraceInfo>comparingInt(info -> info.isSneakRequired() ? 1 : 0)
                .thenComparing(
                    info -> VectorUtils.getCrosshairDistance(eyes, normal, info.getCenteredPos())))
        .orElse(null);
  }

  public static BlockTraceInfo getPlaceableBlockSideTrace(
      Vec3d eyes, Vec3d normal, EnumSet<EnumFacing> sides, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, sides.stream(), pos);
  }

  public static BlockTraceInfo getPlaceableBlockSideTrace(Vec3d eyes, Vec3d normal, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, Stream.of(EnumFacing.values()), pos);
  }

  public static BlockTraceInfo getBlockSideTrace(Vec3d eyes, BlockPos pos, EnumFacing side) {
    return Optional.of(newBlockTrace(pos, side))
        .filter(tr -> BlockHelper.isTraceClear(eyes, tr.getHitVec(), tr.getSide()))
        .filter(tr -> LocalPlayerUtils.isInReach(eyes, tr.getHitVec()))
        .orElse(null);
  }

  public static BlockTraceInfo getVisibleBlockSideTrace(Vec3d eyes, Vec3d normal, BlockPos pos) {
    return Arrays.stream(EnumFacing.values())
        .map(side -> BlockHelper.getBlockSideTrace(eyes, pos, side.getOpposite()))
        .filter(Objects::nonNull)
        .min(
            Comparator.comparingDouble(
                i -> VectorUtils.getCrosshairDistance(eyes, normal, i.getCenteredPos())))
        .orElse(null);
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

    // TODO: update
    public boolean isPlaceable(InvItem item) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("isPlaceable");
      /*if (!(item.getItem() instanceof ItemBlock)) return true;

      ItemBlock itemBlock = (ItemBlock) item.getItem();
      return itemBlock.canPlaceBlockOnSide(
          getWorld(), getPos(), getOppositeSide(), getLocalPlayer(), item.getItemStack());*/
    }

    public boolean isSneakRequired() {
      return BlockActivationChecker.isOverwritten(getBlockState().getBlock());
    }
  }

  public static class UniqueBlock {
    private final IBlockState blockState;
    private final BlockPos pos;

    private UniqueBlock(IBlockState block, BlockPos pos) {
      this.blockState = block;
      this.pos = pos;
    }

    public Block getBlock() {
      return blockState.getBlock();
    }

    public BlockPos getPos() {
      return pos;
    }

    public Vec3d getCenteredPos() {
      return new Vec3d(getPos()).add(getOBBCenter(getPos()));
    }

    public ItemStack asItemStack() {
      return new ItemStack(getBlock(), 1);
    }

    public boolean isInvalid() {
      return Blocks.AIR.equals(getBlock());
    }

    public boolean isEqual(BlockPos pos) {
      IBlockState state = getWorld().getBlockState(pos);
      return Objects.equals(state, this.blockState);
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof UniqueBlock
              && this.blockState.equals(((UniqueBlock) obj).blockState));
    }

    @Override
    public String toString() {
      return Optional.ofNullable(getBlock().getRegistryName())
          .map(Object::toString)
          .orElseGet(() -> getBlock().getNameTextComponent().getUnformattedComponentText());
    }
  }

  public static class BlockActivationChecker {
    private static final Object2BooleanArrayMap<Class<?>> CACHE = new Object2BooleanArrayMap<>();

    public static boolean isOverwritten(final Block instance) {
      Objects.requireNonNull(instance);
      return CACHE.computeIfAbsent(
          instance.getClass(),
          clazz ->
              Block.class
                  != ReflectionHelper.getMethodDeclaringClass(Block_onBlockActivated, instance));
    }
  }
}
