package dev.fiki.forgehax.api.events.render;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;

@Getter
@Setter
@AllArgsConstructor
@Cancelable
public class GuiChangedEvent extends Event {
  private Screen gui;
}
