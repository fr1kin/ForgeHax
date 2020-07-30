package dev.fiki.forgehax.asm.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class BlockControllerProcessEvent extends Event {
  private final Minecraft minecraft;

  @Setter
  private boolean leftClicked;
}
