package dev.fiki.forgehax.asm.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.Event;

@Getter
@AllArgsConstructor
public class PlayerDamageBlockEvent extends Event {
  private final PlayerController playerController;
  private final BlockPos pos;
  private final Direction side;
}
