package dev.fiki.forgehax.api.events.game;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeyInputEvent extends Event {
  private final int key;
  private final int scanCode;
  private final int action;
  private final int modifiers;
}
