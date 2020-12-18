package dev.fiki.forgehax.asm.events.world;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateChunkPositionEvent extends Event {
  private final int ix;
  private final int iy;
  private final int iz;

  private final int x;
  private final int y;
  private final int z;
}
