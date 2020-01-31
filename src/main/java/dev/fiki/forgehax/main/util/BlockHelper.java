package dev.fiki.forgehax.main.util;

import com.google.common.collect.Lists;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.math.VectorUtils;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.reflection.ReflectionHelper;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockHelper {

  @Deprecated
  public static UniqueBlock newUniqueBlock(Block block, int metadata, BlockPos pos) {
    return new UniqueBlock(block, metadata, pos);
  }

  @Deprecated
  public static UniqueBlock newUniqueBlock(Block block, int metadata) {
    return newUniqueBlock(block, metadata, BlockPos.ZERO);
  }

  @Deprecated
  public static UniqueBlock newUniqueBlock(BlockPos pos) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static BlockTraceInfo newBlockTrace(BlockPos pos, Direction side) {
    return new BlockTraceInfo(pos, side);
  }

  @Deprecated
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

  @Deprecated
  public static float getBlockHardness(BlockPos pos) {
    BlockState state = Globals.getWorld().getBlockState(pos);
    return state.getBlockHardness(Globals.getWorld(), pos);
  }

  @Deprecated
  public static boolean isBlockReplaceable(BlockPos pos) {
    return Globals.getWorld().getBlockState(pos).getMaterial().isReplaceable();
  }

  @Deprecated
  public static boolean isTraceClear(Vec3d start, Vec3d end, Direction targetSide) {
//    RayTraceResult tr = getWorld().rayTraceBlocks(start, end, false, true, false);
//    return tr == null
//        || (new BlockPos(end).equals(new BlockPos(tr.hitVec))
//        && targetSide.getOpposite().equals(tr.sideHit));
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static Vec3d getOBBCenter(BlockPos pos) {
    BlockState state = Globals.getWorld().getBlockState(pos);
    AxisAlignedBB bb = state.getCollisionShape(Globals.getWorld(), pos).getBoundingBox();
    return new Vec3d(
        bb.minX + ((bb.maxX - bb.minX) / 2.D),
        bb.minY + ((bb.maxY - bb.minY) / 2.D),
        bb.minZ + ((bb.maxZ - bb.minZ) / 2.D));
  }

  @Deprecated
  public static boolean isBlockPlaceable(BlockPos pos) {
//    BlockState state = getWorld().getBlockState(pos);
//    state.coll
//    return state.getBlock().co(state, false);
    throw new UnsupportedOperationException();
  }

  @Deprecated
  private static BlockTraceInfo getPlaceableBlockSideTrace(
      Vec3d eyes, Vec3d normal, Stream<Direction> stream, BlockPos pos) {
    return stream
        .map(side -> newBlockTrace(pos.offset(side), side))
        .filter(info -> isBlockPlaceable(info.getPos()))
        .filter(info -> LocalPlayerUtils.isInReach(eyes, info.getHitVec()))
        .filter(info -> BlockHelper.isTraceClear(eyes, info.getHitVec(), info.getSide()))
        .min(
            Comparator.<BlockTraceInfo>comparingInt(info -> info.isSneakRequired() ? 1 : 0)
                .thenComparing(
                    info -> VectorUtils.getCrosshairDistance(eyes, normal, info.getCenteredPos())))
        .orElse(null);
  }

  @Deprecated
  public static BlockTraceInfo getPlaceableBlockSideTrace(
      Vec3d eyes, Vec3d normal, EnumSet<Direction> sides, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, sides.stream(), pos);
  }

  @Deprecated
  public static BlockTraceInfo getPlaceableBlockSideTrace(Vec3d eyes, Vec3d normal, BlockPos pos) {
    return getPlaceableBlockSideTrace(eyes, normal, Stream.of(Direction.values()), pos);
  }

  @Deprecated
  public static BlockTraceInfo getBlockSideTrace(Vec3d eyes, BlockPos pos, Direction side) {
    return Optional.of(newBlockTrace(pos, side))
        .filter(tr -> BlockHelper.isTraceClear(eyes, tr.getHitVec(), tr.getSide()))
        .filter(tr -> LocalPlayerUtils.isInReach(eyes, tr.getHitVec()))
        .orElse(null);
  }

  @Deprecated
  public static BlockTraceInfo getVisibleBlockSideTrace(Vec3d eyes, Vec3d normal, BlockPos pos) {
    return Arrays.stream(Direction.values())
        .map(side -> BlockHelper.getBlockSideTrace(eyes, pos, side.getOpposite()))
        .filter(Objects::nonNull)
        .min(
            Comparator.comparingDouble(
                i -> VectorUtils.getCrosshairDistance(eyes, normal, i.getCenteredPos())))
        .orElse(null);
  }

  @Deprecated
  public static class BlockTraceInfo {
    
    private final BlockPos pos;
    private final Direction side;
    private final Vec3d center;
    private final Vec3d hitVec;
    
    private BlockTraceInfo(BlockPos pos, Direction side) {
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
    
    public Direction getSide() {
      return side;
    }
    
    public Direction getOppositeSide() {
      return side.getOpposite();
    }
    
    public Vec3d getHitVec() {
      return this.hitVec;
    }
    
    public Vec3d getCenteredPos() {
      return center;
    }
    
    public BlockState getBlockState() {
      return Globals.getWorld().getBlockState(getPos());
    }
    
    public boolean isPlaceable(LocalPlayerInventory.InvItem item) {
      if (!(item.getItem() instanceof BlockItem)) {
        return true;
      }

      BlockItem itemBlock = (BlockItem) item.getItem();
      return false;
      // TODO: 1.15 find alternative
//      return itemBlock.canPlaceBlockOnSide(getWorld(), getPos(),
//          getOppositeSide(), getLocalPlayer(), item.getItemStack());
    }
    
    public boolean isSneakRequired() {
      return BlockActivationChecker.isOverwritten(getBlockState().getBlock());
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
      throw new UnsupportedOperationException();
    }
    
    public boolean isInvalid() {
      return Blocks.AIR.equals(getBlock());
    }
    
    public boolean isEqual(BlockPos pos) {
//      BlockState state = getWorld().getBlockState(pos);
//      Block bl = state.getBlock();
//      return Objects.equals(getBlock(), bl) && getMetadata() == bl.getMetaFromState(state);
      throw new UnsupportedOperationException();
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
  
  public static class BlockActivationChecker {
    
    private static final Object2BooleanArrayMap<Class<?>> CACHE = new Object2BooleanArrayMap<>();
    
    public static boolean isOverwritten(final Block instance) {
      Objects.requireNonNull(instance);
      return CACHE.computeIfAbsent(
          instance.getClass(),
          clazz -> Block.class != ReflectionHelper.getMethodDeclaringClass(
              FastReflection.Methods.Block_onBlockActivated,
              instance)
      );
    }
  }
}
