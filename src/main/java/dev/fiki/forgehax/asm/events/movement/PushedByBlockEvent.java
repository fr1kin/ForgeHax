package dev.fiki.forgehax.asm.events.movement;


import lombok.AllArgsConstructor;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
@AllArgsConstructor
public class PushedByBlockEvent extends Event {
  private final ClientPlayerEntity player;
}
