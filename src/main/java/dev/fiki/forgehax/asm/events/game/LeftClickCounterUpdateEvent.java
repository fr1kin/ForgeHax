package dev.fiki.forgehax.asm.events.game;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

@Getter
@AllArgsConstructor
@Cancelable
public class LeftClickCounterUpdateEvent extends Event {
  private final Minecraft minecraft;

  @Setter
  private int value;
}
