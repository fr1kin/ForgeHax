package dev.fiki.forgehax.asm.hooks;

import dev.fiki.forgehax.asm.events.world.ChunkRenderRebuildEvent;
import dev.fiki.forgehax.asm.events.world.UpdateChunkPositionEvent;
import dev.fiki.forgehax.asm.events.world.ViewFrustumInitialized;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public class MarkerHooks {
  public static void onRebuildChunk(ChunkRenderDispatcher.ChunkRender chunk, boolean async) {
    ForgeHaxHooks.EVENT_BUS.post(new ChunkRenderRebuildEvent(chunk, async));
  }

  public static void onCreateRenderChunks(ViewFrustum viewFrustum) {
    ForgeHaxHooks.EVENT_BUS.post(new ViewFrustumInitialized(viewFrustum));
  }

  public static void onUpdateChunkPosition(int ix, int iy, int iz, int x, int y, int z) {
    ForgeHaxHooks.EVENT_BUS.post(new UpdateChunkPositionEvent(ix, iy, iz, x, y, z));
  }
}
