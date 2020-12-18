package dev.fiki.forgehax.asm.events.world;

import dev.fiki.forgehax.api.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.ViewFrustum;

@Getter
@RequiredArgsConstructor
public class ViewFrustumInitialized extends Event {
  private final ViewFrustum viewFrustum;
}
