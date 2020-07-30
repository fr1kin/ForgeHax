package dev.fiki.forgehax.asm.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.item.BoatEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

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
