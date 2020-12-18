package dev.fiki.forgehax.api.events.world;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.world.ClientWorld;

@Getter
@RequiredArgsConstructor
public class WorldChangeEvent extends Event {
  private final ClientWorld world;
}
