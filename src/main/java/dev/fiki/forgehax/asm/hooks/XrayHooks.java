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
  private static final int RENDER_OFF = 0;
  private static final int RENDER_TRANSPARENT = 1;
  private static final int RENDER_XRAYED = 2;

  @Setter
  private static boolean xrayBlocks = false;

  private static final ThreadLocal<Integer> rendering = ThreadLocal.withInitial(() -> 0);

  @Setter
  private static float blockAlphaOverride = 1.0f;

  @Setter
  private static boolean fullbright = false;

  @Setter
  private static volatile Predicate<BlockState> shouldXrayBlock = state -> false;

  public static boolean isXrayBlocks() {
    return xrayBlocks;
  }

  public static boolean isRenderingBlock() {
    return rendering.get() > RENDER_OFF;
  }

  public static boolean shouldMakeTransparent(BlockState state) {
    if (!shouldXrayBlock.test(state)) {
      rendering.set(RENDER_TRANSPARENT);
      return true;
    } else {
      rendering.set(RENDER_XRAYED);
      return false;
    }
  }

  public static boolean changeBrightness(int[] brightness, float[] colorMul) {
    final int mode = rendering.get();
    if (fullbright || mode == RENDER_XRAYED) {
      Arrays.fill(brightness, 0xF000F0);
      Arrays.fill(colorMul, 1.f);
      if (mode == RENDER_XRAYED) {
        return false;
      }
    }
    return mode == RENDER_TRANSPARENT;
  }

  public static void blockRenderFinished() {
    // this could create a memory leak if new threads are spawned
    // but i dont want to be constantly removing objects from the map
    rendering.set(RENDER_OFF);
  }

  public static float getBlockAlphaOverride() {
    return blockAlphaOverride;
  }

  public static boolean canRenderInLayerOverride(BlockState state, RenderType type) {
    if (shouldXrayBlock.test(state)) {
      return RenderTypeLookup.canRenderInLayer(state, type);
    } else {
      return type == RenderType.translucent();
    }
  }

  public static boolean shouldSideBeRendered(IBlockReader reader, BlockPos at, BlockState blockState) {
    return shouldXrayBlock.test(reader.getBlockState(at));
  }
}
