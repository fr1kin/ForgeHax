package dev.fiki.forgehax.main.mods;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.events.RenderEvent;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.EnumSetting;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.cmd.settings.LongSetting;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.tesselation.GeometryMasks;
import dev.fiki.forgehax.main.util.tesselation.GeometryTessellator;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 10/12/2017 by fr1kin
 */
@RegisterMod
public class ChunkLogger extends ToggleMod {

  public ChunkLogger() {
    super(Category.MISC, "ChunkLogger", false, "Show new chunks");
  }

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

  @SubscribeEvent
  public void onChunkLoad(ChunkEvent.Load event) {
    if (chunks != null) {
    }
  }

  @SubscribeEvent
  public void onPacketInbound(PacketInboundEvent event) {
    if (event.getPacket() instanceof SChunkDataPacket) {
      SChunkDataPacket packet = (SChunkDataPacket) event.getPacket();
      addChunk(packet);
    }
  }

  @SubscribeEvent
  public void onRender(RenderEvent event) {
    if (chunks == null) {
      return;
    }

    event.getTessellator().beginLines();

    List<ChunkData> copy;
    chunkLock.lock();
    try {
      copy = Lists.newArrayList(chunks);
    } finally {
      chunkLock.unlock();
    }

    copy.forEach(
        chunk -> {
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

          int color = chunk.isNewChunk() ? Colors.WHITE.toBuffer() : Colors.RED.toBuffer();

          GeometryTessellator.drawQuads(
              event.getBuffer(),
              chunk.bbox.minX,
              chunk.bbox.minY,
              chunk.bbox.minZ,
              chunk.bbox.maxX,
              chunk.bbox.maxY,
              chunk.bbox.maxZ,
              GeometryMasks.Quad.ALL,
              color);
        });

    event.getTessellator().draw();
  }

  private interface ChunkLoadThread extends Callable<Object> {

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
      pos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
      bbox =
          new AxisAlignedBB(pos.getXStart(), 0, pos.getZStart(), pos.getXEnd(), 255, pos.getZEnd());
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
