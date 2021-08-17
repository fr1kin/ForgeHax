package dev.fiki.forgehax.api.marker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.Arrays;

@RequiredArgsConstructor
public class MarkerWorker {
  final MarkerDispatcher dispatcher;

  @Getter
  final VertexBuffer vertexBuffer = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
  @Getter
  final BlockPos.Mutable position = new BlockPos.Mutable(-1, -1, -1);
  final BlockPos.Mutable[] mapEnumFacing = Util.make(new BlockPos.Mutable[6],
      poses -> Arrays.setAll(poses, i -> new BlockPos.Mutable()));

  MarkerJob lastChunkJob = null;
  AxisAlignedBB boundingBox;
  boolean needsUpdate = false;
  boolean needsImmediateUpdate = false;

  @Getter
  boolean isEmpty = true;

  private boolean isChunkLoaded(BlockPos pos) {
    return dispatcher.getWorld().getChunk(pos.getX() >> 4, pos.getZ() >> 4,
        ChunkStatus.FULL, false) != null;
  }

  public void setPosition(int x, int y, int z) {
    if (x != this.position.getX() || y != this.position.getY() || z != this.position.getZ()) {
      stop();

      this.position.set(x, y, z);
      this.boundingBox = new AxisAlignedBB(x, y, z, x + 16, y + 16, z + 16);

      for (Direction direction : Direction.values()) {
        this.mapEnumFacing[direction.ordinal()].set(this.position).move(direction, 16);
      }
    }
  }

  protected double getDistanceSq() {
    ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getMainCamera();
    double d0 = this.boundingBox.minX + 8.0D - info.getPosition().x;
    double d1 = this.boundingBox.minY + 8.0D - info.getPosition().y;
    double d2 = this.boundingBox.minZ + 8.0D - info.getPosition().z;
    return d0 * d0 + d1 * d1 + d2 * d2;
  }

  public boolean shouldStayLoaded() {
    if (!(this.getDistanceSq() > 576.0D)) {
      return true;
    } else {
      return this.isChunkLoaded(this.mapEnumFacing[Direction.WEST.ordinal()])
          && this.isChunkLoaded(this.mapEnumFacing[Direction.NORTH.ordinal()])
          && this.isChunkLoaded(this.mapEnumFacing[Direction.EAST.ordinal()])
          && this.isChunkLoaded(this.mapEnumFacing[Direction.SOUTH.ordinal()]);
    }
  }

  public boolean isNeedsUpdate() {
    return needsUpdate;
  }

  public boolean isNeedsImmediateUpdate() {
    return needsUpdate && needsImmediateUpdate;
  }

  public void needsUpdate(boolean immediate) {
    boolean flag = this.needsUpdate;
    this.needsUpdate = true;
    this.needsImmediateUpdate = immediate | (flag && this.needsImmediateUpdate);
  }

  public void clearNeedsUpdate() {
    this.needsUpdate = this.needsImmediateUpdate = false;
  }

  public void scheduleUpdate() {
    dispatcher.schedule(createMarkerChunk());
  }

  public void stop() {
    if (lastChunkJob != null) {
      lastChunkJob.cancel();
      lastChunkJob = null;
      isEmpty = true;
    }
    needsUpdate(false);
  }

  public void deleteGlResources() {
    vertexBuffer.close();
  }

  public MarkerJob createMarkerChunk() {
    isEmpty = true;
    return this.lastChunkJob = new MarkerJob(this, getDistanceSq());
  }
}
