package dev.fiki.forgehax.asm.events.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraftforge.eventbus.api.Event;

@Getter
@RequiredArgsConstructor
public class ViewFrustumInitialized extends Event {
  private final ViewFrustum viewFrustum;
}
