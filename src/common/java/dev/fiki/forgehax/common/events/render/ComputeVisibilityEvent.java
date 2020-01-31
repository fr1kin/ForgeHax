package dev.fiki.forgehax.common.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class ComputeVisibilityEvent extends Event {
  private final VisGraph visGraph;
  private final SetVisibility setVisibility;
}
