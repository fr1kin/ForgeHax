package dev.fiki.forgehax.main.mods.misc;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.GeometryMasks;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RegisterMod(
    name = "ChunkLogger",
    description = "Show new chunks",
    category = Category.MISC
)
@ExtensionMethod({VertexBuilderEx.class})
public class ChunkLogger extends ToggleMod {
  enum ShowChunkEnum {
    ALL,
    NEW_ONLY,
    OLD_ONLY,
    ;
  }

  enum DetectionMethodEnum {
    IS_FULL_CHUNK,
    TIMING,
    BLOCK_CHANGE_THRESHOLD,
    DECORATOR_BLOCKS_DETECTED,
  }

  private final IntegerSetting max_chunks = newIntegerSetting()
      .name("max-chunks")
      .description("Maximum chunks to render (set to 0 for infinite)")
      .defaultTo(5120)
      .build();

  private final BooleanSetting clear_on_toggle = newBooleanSetting()
      .name("clear-on-toggle")
      .description("Clear chunk list on disable")
      .defaultTo(true)
      .build();

  private final EnumSetting<ShowChunkEnum> show_only = newEnumSetting(ShowChunkEnum.class)
      .name("show-only")
      .description("Specify which chunk to only show")
      .defaultTo(ShowChunkEnum.ALL)
      .build();

  private final EnumSetting<DetectionMethodEnum> detection_method = newEnumSetting(DetectionMethodEnum.class)
      .name("detection-method")
      .description("Specify the method to detect new chunks. Currently only IS_FULL_CHUNK is supported.")
      .defaultTo(DetectionMethodEnum.IS_FULL_CHUNK)
      .build();

  private final LongSetting flag_timing = newLongSetting()
      .name("flag-timing")
      .description("Maximum time in MS that another chunk load in succession will trigger"
          + " it to be marked as a new chunk")
      .defaultTo(1000L)
      .build();

  private final IntegerSetting block_change_threshold = newIntegerSetting()
      .name("block-change-threshold")
      .description("Maximum number of blocks required to change between chunk loading"
          + " in order to be marked as a new chunk")
      .defaultTo(100)
      .build();

  private final Lock chunkLock = new ReentrantLock();

  private Queue<ChunkData> chunks = null;

  void addChunk(SChunkDataPacket packet) {
    if (chunks != null) {
      chunkLock.lock();
      try {
        ChunkData temp = new ChunkData(packet);
        ChunkData data = chunks.stream().filter(temp::equals).findAny().orElse(null);
        if (data != null) {
          data.update(packet);
          chunks.remove(data);
          chunks.add(data); // remove and re-add to bring forward in the queue
        } else {
          chunks.add(temp);
        }
      } finally {
        chunkLock.unlock();
      }
    }
  }

  @Override
  protected void onEnabled() {
    chunkLock.lock();
    try {
      if (max_chunks.getValue() <= 0) {
        chunks = Queues.newArrayDeque();
      } else {
        chunks = EvictingQueue.create(max_chunks.getValue());
      }
    } finally {
      chunkLock.unlock();
    }
  }

  @Override
  protected void onDisabled() {
    if (clear_on_toggle.getValue() && chunks != null) {
      chunkLock.lock();
      try {
        chunks.clear();
        chunks = null;
      } finally {
        chunkLock.unlock();
      }
    }
  }

  @SubscribeListener
  public void onPacketInbound(PacketInboundEvent event) {
    if (event.getPacket() instanceof SChunkDataPacket) {
      SChunkDataPacket packet = (SChunkDataPacket) event.getPacket();
      addChunk(packet);
    }
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    if (chunks == null) {
      return;
    }

    BufferBuilder builder = event.getBuffer();
    builder.beginLines(DefaultVertexFormats.POSITION_COLOR);

    List<ChunkData> copy;
    chunkLock.lock();
    try {
      copy = Lists.newArrayList(chunks);
    } finally {
      chunkLock.unlock();
    }

    copy.forEach(chunk -> {
      switch (show_only.getValue()) {
        case NEW_ONLY:
          if (!chunk.isNewChunk()) {
            return;
          }
          break;
        case OLD_ONLY:
          if (chunk.isNewChunk()) {
            return;
          }
          break;
        case ALL:
        default:
          break;
      }

      builder.outlinedCube(chunk.bbox, GeometryMasks.Quad.ALL,
          chunk.isNewChunk() ? Colors.WHITE : Colors.RED, null);
    });

    builder.draw();
  }

  private class ChunkData {

    final ChunkPos pos;
    final AxisAlignedBB bbox;
    final boolean isFullChunk; // initial chunk

    boolean isNewByFullChunk = false;
    boolean isNewByTiming = false;
    boolean isNewByBlockCount = false;
    boolean isNewByDecoratorBlocks = false;

    boolean updatedIsFullChunk;

    long timeArrived = -1;
    long previousTimeArrived;

    int blockCount = 0;
    int previousBlockCount;

    ChunkData(SChunkDataPacket packet) {
      pos = new ChunkPos(packet.getX(), packet.getZ());
      bbox =
          new AxisAlignedBB(pos.getMinBlockX(), 0, pos.getMinBlockZ(), pos.getMaxBlockX(), 255, pos.getMaxBlockZ());
      isFullChunk = packet.isFullChunk();
      update(packet);
    }

    void update(SChunkDataPacket packet) {
      updatedIsFullChunk = packet.isFullChunk();
      if (!updatedIsFullChunk) {
        isNewByFullChunk = true;
      }

      previousTimeArrived = timeArrived;
      timeArrived = System.currentTimeMillis();

      if (getTimeDifference() != -1 && getTimeDifference() <= flag_timing.getValue()) {
        isNewByTiming = true;
      }

      previousBlockCount = blockCount;
    }

    long getTimeDifference() {
      return previousTimeArrived == -1 ? -1 : previousTimeArrived - timeArrived;
    }

    boolean isNewChunk() {
      switch (detection_method.getValue()) {
        case IS_FULL_CHUNK:
          return isNewByFullChunk;
        case TIMING:
          return isNewByTiming;
      }
      return false;
    }

    @Override
    public boolean equals(Object obj) {
      return obj == this
          || (obj instanceof ChunkData
          && this.pos.x == ((ChunkData) obj).pos.x
          && this.pos.z == ((ChunkData) obj).pos.z);
    }

    @Override
    public int hashCode() {
      return Objects.hash(pos.x, pos.z);
    }
  }
}
