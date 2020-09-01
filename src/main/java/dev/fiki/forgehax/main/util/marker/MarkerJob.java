package dev.fiki.forgehax.main.util.marker;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.GeometryMasks;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
public class MarkerJob implements Comparable<MarkerJob> {
  private final MarkerWorker worker;
  private final double distanceSq;

  private final AtomicBoolean finished = new AtomicBoolean(false);

  public VertexBuffer getVertexBuffer() {
    return worker.vertexBuffer;
  }

  public CompletableFuture<Boolean> execute(BufferBuilderEx buffer) {
    if (finished.get()) {
      return CompletableFuture.completedFuture(false);
    } else if (!worker.shouldStayLoaded()) {
      worker.needsUpdate(false);
      finished.set(true);
      return CompletableFuture.completedFuture(false);
    }

    final Vector3d renderPos = worker.dispatcher.renderPosition;
    final BlockPos start = worker.position.toImmutable();
    final BlockPos end = start.add(15, 15, 15);
    final World world = worker.dispatcher.getWorld();
    final Function<BlockState, Color> blockToColor = worker.dispatcher.blockToColor;

    MatrixStack stack = new MatrixStack();
    boolean startedDrawing = false;

    buffer.setMatrixStack(stack);

    for (BlockPos pos : BlockPos.getAllInBoxMutable(start, end)) {
      BlockState state = world.getBlockState(pos);
      Color color = blockToColor.apply(state);
      if (color != null) {
        Block block = state.getBlock();

        if (!startedDrawing) {
          buffer.beginLines(DefaultVertexFormats.POSITION_COLOR);
          startedDrawing = true;
          worker.isEmpty = false;
        }

        stack.push();
        stack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

        AxisAlignedBB bb = state.getShape(world, pos).getBoundingBox();
        AxisAlignedBB box = bb.offset(pos);

        int flags = GeometryMasks.Line.ALL;
        for (Direction dir : Direction.values()) {
          BlockPos otherPos = pos.offset(dir);
          BlockState other = world.getBlockState(otherPos);
          if (block.equals(other.getBlock())) {
            AxisAlignedBB otherBox = other.getShape(world, otherPos).getBoundingBox().offset(otherPos);
            double max = box.getMax(dir.getAxis());
            double min = box.getMin(dir.getAxis());
            double otherMax = otherBox.getMax(dir.getAxis());
            double otherMin = otherBox.getMin(dir.getAxis());
            if (max == otherMax || max == otherMin || min == otherMax || min == otherMin || box.intersects(otherBox)) {
              flags &= ~GeometryMasks.Line.getFlagForDirection(dir);
            }
          }
        }

        buffer.putOutlinedCuboid(bb, flags, color);

        stack.pop();
      }
    }

    if (startedDrawing) {
      buffer.finishDrawing();
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
