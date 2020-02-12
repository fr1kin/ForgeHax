package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class LeftClickCounterUpdateEvent extends Event {
  private final Minecraft minecraft;

  @Setter
  private int value;
}
