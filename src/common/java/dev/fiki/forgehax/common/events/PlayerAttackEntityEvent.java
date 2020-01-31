package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class PlayerAttackEntityEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity attacker;
  private final Entity victim;
}
