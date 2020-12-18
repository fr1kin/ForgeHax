package dev.fiki.forgehax.asm.events.boat;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.item.BoatEntity;

/**
 * Created by Babbaj on 9/2/2017.
 */
@Getter
@AllArgsConstructor
@Cancelable
public class RenderBoatEvent extends Event {
  private final BoatEntity boat;

  @Setter
  private float yaw;
}
