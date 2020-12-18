package dev.fiki.forgehax.asm.events.player;

import dev.fiki.forgehax.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Getter
@AllArgsConstructor
public class PlayerAttackEntityEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity attacker;
  private final Entity victim;
}
