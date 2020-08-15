package dev.fiki.forgehax.asm.hooks;

import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.Arrays;
import java.util.function.Predicate;

public class XrayHooks {
  @Setter
  private static boolean xrayBlocks = false;

  private static final ThreadLocal<Boolean> renderingBlock = ThreadLocal.withInitial(() -> false);
  private static final ThreadLocal<Boolean> xrayingBlock = ThreadLocal.withInitial(() -> false);

  @Setter
  private static float blockAlphaOverride = 1.0f;

  @Setter
  private static int blockLightmapOverride = 12582912;

  @Setter
  private static volatile Predicate<BlockState> shouldXrayBlock = state -> false;

  public static boolean isXrayBlocks() {
    return xrayBlocks;
  }

  public static boolean isRenderingBlock() {
    return renderingBlock.get();
  }

  public static boolean shouldMakeTransparent(BlockState state) {
    if (!shouldXrayBlock.test(state)) {
      renderingBlock.set(true);
      return true;
    } else {
      xrayingBlock.set(true);
      return false;
    }
  }

  public static boolean changeBrightness(int[] brightness, float[] colorMul) {
    if (xrayingBlock.get()) {
      Arrays.fill(brightness, blockLightmapOverride);
      Arrays.fill(colorMul, 1.f);
      return false;
    }
    return renderingBlock.get();
  }

  public static void blockRenderFinished() {
    // this could create a memory leak if new threads are spawned
    // but i dont want to be constantly removing objects from the map
    renderingBlock.set(false);
    xrayingBlock.set(false);
  }

  public static float getBlockAlphaOverride() {
    return blockAlphaOverride;
  }

  public static boolean canRenderInLayerOverride(BlockState state, RenderType type) {
    if (shouldXrayBlock.test(state)) {
      return RenderTypeLookup.canRenderInLayer(state, type);
    } else {
      return type == RenderType.getTranslucent();
    }
  }

  public static boolean shouldSideBeRendered(IBlockReader reader, BlockPos at, BlockState blockState) {
    return shouldXrayBlock.test(reader.getBlockState(at));
  }
}
