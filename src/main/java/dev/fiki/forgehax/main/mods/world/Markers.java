package dev.fiki.forgehax.main.mods.world;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.argument.Arguments;
import dev.fiki.forgehax.api.cmd.listener.Listeners;
import dev.fiki.forgehax.api.cmd.settings.maps.SimpleSettingMap;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.api.events.render.RenderSpaceEvent;
import dev.fiki.forgehax.api.marker.MarkerDispatcher;
import dev.fiki.forgehax.api.marker.MarkerWorker;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.modloader.di.Injected;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.render.CullCavesEvent;
import dev.fiki.forgehax.asm.events.world.ChunkRenderRebuildEvent;
import dev.fiki.forgehax.asm.events.world.UpdateChunkPositionEvent;
import dev.fiki.forgehax.asm.events.world.ViewFrustumInitialized;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "Markers",
    description = "Draw boxes over specific blocks",
    category = Category.WORLD
)
@RequiredArgsConstructor
public class Markers extends ToggleMod implements Common {
  @Injected("threadpool")
  private final ExecutorService pool;

  @MapField(parentClass = ViewFrustum.class, value = "countChunksX")
  private final ReflectionField<Integer> ViewFrustum_countChunksX;
  @MapField(parentClass = ViewFrustum.class, value = "countChunksY")
  private final ReflectionField<Integer> ViewFrustum_countChunksY;
  @MapField(parentClass = ViewFrustum.class, value = "countChunksZ")
  private final ReflectionField<Integer> ViewFrustum_countChunksZ;

  @MapField(parentClass = WorldRenderer.class, value = "world")
  private final ReflectionField<ClientWorld> WorldRenderer_world;
  @MapField(parentClass = WorldRenderer.class, value = "viewFrustum")
  private final ReflectionField<ViewFrustum> WorldRenderer_viewFrustum;

  private final SimpleSettingMap<Block, Color> blocks = newSettingMap(Block.class, Color.class)
      .name("blocks")
      .description("Blocks to enable markers for")
      .supplier(Maps::newConcurrentMap)
      .keyArgument(Arguments.newBlockArgument()
          .label("block")
          .build())
      .valueArgument(Arguments.newColorArgument()
          .label("color")
          .defaultValue(Colors.WHITE)
          .optional()
          .build())
      .listener(Listeners.onUpdate(o -> reloadWorldChunks()))
      .build();

  {
    blocks.newSimpleCommand()
        .name("bulk-add")
        .description("Add all matching a given string.")
        .argument(Arguments.newStringArgument()
            .label("search blocks")
            .maxArgumentsConsumed(1)
            .build())
        .argument(Arguments.newColorArgument()
            .label("color")
            .defaultValue(Colors.WHITE)
            .optional()
            .build())
        .executor(args -> {
          final String searchString = args.<String>getFirst().getValue();
          final String lower = searchString.toLowerCase();
          final Color color = args.<Color>getSecond().getValue();

          Set<Block> blocks = StreamSupport.stream(getBlockRegistry().spliterator(), false)
              .filter(block -> Blocks.AIR != block)
              .filter(block -> block.getRegistryName() != null)
              .filter(block -> block.getRegistryName().toString().toLowerCase().contains(lower))
              .collect(Collectors.toSet());

          if (blocks.isEmpty()) {
            args.warn("Found no blocks matching name %s", searchString);
          } else {
            args.inform("Adding blocks %s with color %s",
                blocks.stream()
                    .map(Block::getRegistryName)
                    .map(ResourceLocation::toString)
                    .collect(Collectors.joining(", ")),
                args.getSecond().getStringValue());
            blocks.forEach(block -> this.blocks.put(block, color));
          }
        })
        .build();
  }

  private MarkerDispatcher dispatcher = null;
  private MarkerWorker[] workers = new MarkerWorker[0];

  private int chunksX = 0;
  private int chunksY = 16;
  private int chunksZ = 0;

  private void reloadWorldChunks() {
    if (isEnabled() && isInWorld()) {
      reloadChunkSmooth();
    }
  }

  private void loadMarkers(ViewFrustum viewFrustum) {
    unloadMarkers();

    dispatcher = new MarkerDispatcher(pool);
    dispatcher.setWorld(WorldRenderer_world.get(getWorldRenderer()));
    dispatcher.setBlockToColor(state -> blocks.get(state.getBlock()));

    workers = new MarkerWorker[viewFrustum.renderChunks.length];

    for (int i = 0; i < workers.length; i++) {
      ChunkRenderDispatcher.ChunkRender chunkRender = viewFrustum.renderChunks[i];
      BlockPos pos = chunkRender.getPosition().toImmutable();

      MarkerWorker worker = workers[i] = new MarkerWorker(dispatcher);
      worker.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    chunksX = ViewFrustum_countChunksX.get(viewFrustum);
    chunksY = ViewFrustum_countChunksY.get(viewFrustum);
    chunksZ = ViewFrustum_countChunksZ.get(viewFrustum);
  }

  private void unloadMarkers() {
    if (dispatcher != null) {
      dispatcher.kill();
      dispatcher = null;
    }

    for (MarkerWorker worker : workers) {
      worker.deleteGlResources();
    }

    workers = new MarkerWorker[0];

    chunksX = chunksY = chunksZ = 0;
  }

  private int getWorkerIndex(int x, int y, int z) {
    return (z * chunksY + y) * chunksX + x;
  }

  private MarkerWorker getWorker(int x, int y, int z) {
    int xx = MathHelper.intFloorDiv(x, 16);
    int yy = MathHelper.intFloorDiv(y, 16);
    int zz = MathHelper.intFloorDiv(z, 16);
    if (yy >= 0 && yy < chunksY) {
      xx = MathHelper.normalizeAngle(xx, chunksX);
      zz = MathHelper.normalizeAngle(zz, chunksZ);
      return workers[getWorkerIndex(xx, yy, zz)];
    } else {
      return null;
    }
  }

  private ViewFrustum getViewFrustum() {
    return WorldRenderer_viewFrustum.get(getWorldRenderer());
  }

  @Override
  protected void onEnabled() {
    if (isInWorld()) {
      loadMarkers(getViewFrustum());
      reloadWorldChunks();
    }
  }

  @Override
  protected void onDisabled() {
    addScheduledTask(this::unloadMarkers);
  }

  @SubscribeListener
  public void onDisconnect(DisconnectFromServerEvent event) {
    onDisabled();
  }

  @SubscribeListener
  public void onCullCaves(CullCavesEvent event) {
    event.setCanceled(true);
  }

  @SubscribeListener
  public void onFrustumInit(ViewFrustumInitialized event) {
    loadMarkers(event.getViewFrustum());
  }

  @SubscribeListener
  public void onChunkPositionUpdate(UpdateChunkPositionEvent event) {
    int i = getWorkerIndex(event.getIx(), event.getIy(), event.getIz());
    if (i < workers.length) {
      MarkerWorker worker = workers[i];

      if (worker != null) {
        worker.setPosition(event.getX(), event.getY(), event.getZ());
      } else {
        log.warn("Could not update chunk region {} {} {}", event.getX(), event.getY(), event.getZ());
      }
    }
  }

  @SubscribeListener
  public void onRebuildChunk(ChunkRenderRebuildEvent event) {
    if (dispatcher == null) {
      return;
    }

    BlockPos pos = event.getChunk().getPosition().toImmutable();
    MarkerWorker worker = getWorker(pos.getX(), pos.getY(), pos.getZ());

    if (worker != null) {
      worker.scheduleUpdate();
    } else {
      log.warn("No worker for chunk @ {}", pos);
    }
  }

  @SubscribeListener
  public void onRender(RenderSpaceEvent event) {
    if (dispatcher == null) {
      return;
    }

    MatrixStack stack = event.getStack();
    Vector3d vec = event.getProjectedPos();

    dispatcher.updateChunks();
    dispatcher.setRenderPosition(vec);

    for (MarkerWorker worker : workers) {
      if (!worker.isEmpty()) {
        BlockPos pos = worker.getPosition().toImmutable();
        stack.push();
        stack.translate((double) pos.getX() - vec.getX(),
            (double) pos.getY() - vec.getY(),
            (double) pos.getZ() - vec.getZ());

        worker.getVertexBuffer().bindBuffer();
        DefaultVertexFormats.POSITION_COLOR.setupBufferState(0L);
        worker.getVertexBuffer().draw(stack.getLast().getMatrix(), GL11.GL_LINES);

        stack.pop();
      }
    }

    VertexBuffer.unbindBuffer();
    RenderSystem.clearCurrentColor();
    DefaultVertexFormats.POSITION_COLOR.clearBufferState();
  }
}
