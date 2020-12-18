package dev.fiki.forgehax.asm.events.world;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Getter
@RequiredArgsConstructor
public class ChunkRenderRebuildEvent extends Event {
  private final ChunkRenderDispatcher.ChunkRender chunk;
  private final boolean async;
}
