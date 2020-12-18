package dev.fiki.forgehax.api.events.game;

import dev.fiki.forgehax.api.event.Cancelable;
import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

@Getter
@Setter
@AllArgsConstructor
@Cancelable
public class EntitySoundEvent extends Event {
  private final Entity entity;
  private SoundEvent sound;
  private SoundCategory category;
  private float volume;
  private float pitch;
}
