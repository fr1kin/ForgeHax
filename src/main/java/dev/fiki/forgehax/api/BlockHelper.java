package dev.fiki.forgehax.api;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.api.extension.GeneralEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.math.VectorUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getWorld;

public class BlockHelper {
  @Deprecated
  public static UniqueBlock newUniqueBlock(Block block, int metadata, BlockPos pos) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static UniqueBlock newUniqueBlock(Block block, int metadata) {
    throw new UnsupportedOperationException();
  }

  public static UniqueBlock newUniqueBlock(BlockPos pos) {
    return new UniqueBlock(getWorld().getBlockState(pos).getBlock(), pos);
  }

  public static Set<Block> getBlocksMatching(Iterable<Block> blocks, String match) {
    final Pattern pattern = Pattern.compile(GeneralEx.globToRegex(match), Pattern.CASE_INSENSITIVE);
    return StreamSupport.stream(blocks.spliterator(), false)
        .filter(block -> block != Blocks.AIR)
        .filter(block -> block.getRegistryName() != null)
        .filter(block -> pattern.matcher(getBlockRegistryName(block)).matches())
        .collect(Collectors.toSet());
  }

  public static String getBlockRegistryName(Block block) {
    return block.getRegistryName().toString();
  }

  public static BlockTraceInfo newBlockTrace(BlockPos pos, Direction side) {
    return new BlockTraceInfo(pos, side);
  }

  public static List<BlockPos> getBlocksInRadius(Vector3d pos, double radius) {
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

  @Deprecated
  public static float getBlockHardness(BlockPos pos) {
    BlockState state = getWorld().getBlockState(pos);
    return state.getDestroySpeed(getWorld(), pos);
  }

  public static boolean isBlockReplaceable(BlockPos pos) {
    return getWorld().getBlockState(pos).getMaterial().isReplaceable();
  }

  public static boolean isTraceClear(Vector3d start, Vector3d end, BlockPos target) {
    RayTraceContext ctx = new RayTraceContext(start, end,
        RayTraceContext.BlockMode.OUTLINE,
        RayTraceContext.FluidMode.NONE, getLocalPlayer());
    BlockRayTraceResult tr = getWorld().clip(ctx);
    return tr.getBlockPos().equals(target);
  }

  public static boolean doesTraceHitBlockSide(Vector3d start, Vector3d end, BlockPos target, Direction direction) {
    Vector3d dir = end.subtract(start).normalize();
    // extend the end vector to reach out more so it actually touches the block
    RayTraceContext ctx = new RayTraceContext(start, end.add(dir.scale(2)),
        RayTraceContext.BlockMode.OUTLINE,
        RayTraceContext.FluidMode.NONE, getLocalPlayer());
    BlockRayTraceResult tr = getWorld().clip(ctx);
    return tr.getBlockPos().equals(target)
        && tr.getDirection().equals(direction);
  }

  public static Vector3d getOBBCenter(BlockPos pos) {
    BlockState state = getWorld().getBlockState(pos);
    VoxelShape shape = state.getCollisionShape(getWorld(), pos);
    if (!shape.isEmpty()) {
      AxisAlignedBB bb = shape.bounds();
      return new Vector3d(
          bb.minX + ((bb.maxX - bb.minX) / 2.D),
          bb.minY + ((bb.maxY - bb.minY) / 2.D),
          bb.minZ + ((bb.maxZ - bb.minZ) / 2.D));
    } else {
      return Vector3d.ZERO;
    }
  }

  public static boolean isBlockPlaceable(BlockPos pos) {
    return !getWorld().getBlockState(pos).getShape(getWorld(), pos).isEmpty();
  }

  public static boolean isItemBlockPlaceable(Item item) {
    if (item instanceof BlockItem) {
      BlockItem blockItem = (BlockItem) item;
      return blockItem.getBlock().defaultBlockState().isCollisionShapeFullBlock(getWorld(), BlockPos.ZERO);
    }
    return false;
  }

  private static BlockTraceInfo getPlaceableBlockSideTrace(Vector3d eyes, Vector3d normal,
      Stream<Direction> stream, BlockPos pos) {
    return stream
        .map(side -> new BlockTraceInfo(pos.relative(side), side))
        .filter(info -> isBlockPlaceable(info.getPos()))
        .filter(info -> LocalPlayerEx.isInReach(null, eyes, info.getHitVec()))
        .filter(info -> BlockHelper.doesTraceHitBlockSide(eyes, info.getHitVec(), info.getPos(), info.getOppositeSide()))
        .min(Comparator.<BlockTraceInfo>comparingInt(info -> info.isSneakRequired() ? 1 : 0)
            .thenComparing(info -> VectorUtil.getCrosshairDistance(eyes, normal, info.getCenterPos())))
        .orElse(null);
  }

  public static BlockTraceInfo getPlaceableBlockSideTrace(Vector3d eyes, Vector3d normal,
      EnumSet<Direction> sides, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, sides.stream(), pos);
  }

  public static BlockTraceInfo getPlaceableBlockSideTrace(Vector3d eyes, Vector3d normal, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, Stream.of(Direction.values()), pos);
  }

  public static BlockTraceInfo getBlockSideTrace(Vector3d eyes, BlockPos pos, Direction side) {
    return Optional.of(newBlockTrace(pos, side))
        .filter(tr -> BlockHelper.doesTraceHitBlockSide(eyes, tr.getHitVec(), tr.getPos(), side.getOpposite()))
        .filter(tr -> LocalPlayerEx.isInReach(null, eyes, tr.getHitVec()))
        .orElse(null);
  }

  public static BlockTraceInfo getVisibleBlockSideTrace(Vector3d eyes, Vector3d normal, BlockPos pos) {
    return Arrays.stream(Direction.values())
        .map(side -> BlockHelper.getBlockSideTrace(eyes, pos, side))
        .filter(Objects::nonNull)
        .min(Comparator.comparingDouble(i -> VectorUtil.getCrosshairDistance(eyes, normal, i.getCenterPos())))
        .orElse(null);
  }

  @Getter
  public static class BlockTraceInfo {
    private final BlockPos pos;
    private final Direction side;
    private final Vector3d centerPos;
    private final Vector3d hitVec;

    private BlockTraceInfo(BlockPos pos, Direction side) {
      this.pos = pos;
      this.side = side;
      Vector3d obb = BlockHelper.getOBBCenter(pos);
      this.centerPos = VectorUtil.toFPIVector(pos).add(obb);
      this.hitVec = this.centerPos.add(obb.multiply(VectorUtil.toFPIVector(getOppositeSide().getNormal())));
    }

    public Direction getOppositeSide() {
      return side.getOpposite();
    }

    public BlockState getBlockState() {
      return getWorld().getBlockState(getPos());
    }

    public boolean isSneakRequired() {
      return false; // TODO: 1.16
    }
  }

  @Getter
  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  public static class UniqueBlock {
    private final Block block;
    @EqualsAndHashCode.Exclude
    private final BlockPos pos;

    public Vector3d getCenteredPos() {
      return VectorUtil.toFPIVector(getPos()).add(getOBBCenter(getPos()));
    }

    public ItemStack asItemStack() {
      throw new UnsupportedOperationException();
    }

    public boolean isInvalid() {
      return Blocks.AIR.equals(getBlock());
    }

    public boolean isEqual(BlockPos pos) {
      return getWorld().getBlockState(pos).getBlock().equals(block);
    }
  }
}
