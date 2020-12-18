package dev.fiki.forgehax.asm.events.game;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

@Getter
@AllArgsConstructor
public class BlockControllerProcessEvent extends Event {
  private final Minecraft minecraft;

  @Setter
  private boolean leftClicked;
}
