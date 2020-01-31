package dev.fiki.forgehax.common.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
@Cancelable
public class ItemStoppedUsedEvent extends Event {
  private final PlayerController playerController;
  private final PlayerEntity player;
}
