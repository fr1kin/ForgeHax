package dev.fiki.forgehax.api.marker;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@RequiredArgsConstructor
@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
public class MarkerJob implements Comparable<MarkerJob> {
  private final MarkerWorker worker;
  private final double distanceSq;

  private final AtomicBoolean finished = new AtomicBoolean(false);

  public VertexBuffer getVertexBuffer() {
    return worker.vertexBuffer;
  }

  public CompletableFuture<Boolean> execute(BufferBuilder buffer) {
    if (finished.get()) {
      return CompletableFuture.completedFuture(false);
    } else if (!worker.shouldStayLoaded()) {
      worker.needsUpdate(false);
      finished.set(true);
      return CompletableFuture.completedFuture(false);
    }

    Vector3d renderPos = worker.dispatcher.renderPosition;
    BlockPos start = worker.position.immutable();
    BlockPos end = start.offset(15, 15, 15);
    World world = worker.dispatcher.getWorld();
    Function<BlockState, Color> blockToColor = worker.dispatcher.blockToColor;
    MatrixStack stack = new MatrixStack();
    boolean startedDrawing = false;

    for (BlockPos pos : BlockPos.betweenClosed(start, end)) {
      val state = world.getBlockState(pos);
      val color = blockToColor.apply(state);
      if (color != null) {
        val block = state.getBlock();

        if (!startedDrawing) {
          buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);
          startedDrawing = true;
          worker.isEmpty = false;
        }

        stack.pushPose();
        stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

        AxisAlignedBB bb = state.getShape(world, pos).bounds();
        AxisAlignedBB box = bb.move(pos);

        int flags = GeometryMasks.Line.ALL;
        for (Direction dir : Direction.values()) {
          BlockPos otherPos = pos.relative(dir);
          BlockState other = world.getBlockState(otherPos);
          if (block.equals(other.getBlock())) {
            AxisAlignedBB otherBox = other.getShape(world, otherPos).bounds().move(otherPos);
            double max = box.max(dir.getAxis());
            double min = box.min(dir.getAxis());
            double otherMax = otherBox.max(dir.getAxis());
            double otherMin = otherBox.min(dir.getAxis());
            if (max == otherMax || max == otherMin || min == otherMax || min == otherMin || box.intersects(otherBox)) {
              flags &= ~GeometryMasks.Line.getFlagForDirection(dir);
            }
          }
        }

        buffer.outlinedCube(bb, flags, color, stack.getLastMatrix());
        stack.popPose();
      }
    }

    if (startedDrawing) {
      buffer.end();
      return CompletableFuture.runAsync(() -> {
      }, worker.dispatcher.uploadTasks::add)
          .thenCompose(v -> getVertexBuffer().uploadLater(buffer))
          .handle((v, ex) -> !finished.get());
    }

    return CompletableFuture.completedFuture(false);
  }

  public void cancel() {
    if (this.finished.compareAndSet(false, true)) {
      worker.needsUpdate(true);
    }
  }

  @Override
  public int compareTo(MarkerJob o) {
    return Double.compare(distanceSq, o.distanceSq);
  }
}
