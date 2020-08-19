package dev.fiki.forgehax.asm.events.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraftforge.eventbus.api.Event;

@Getter
@RequiredArgsConstructor
public class ChunkRenderRebuildEvent extends Event {
  private final ChunkRenderDispatcher.ChunkRender chunk;
  private final boolean async;
}
