package dev.fiki.forgehax.asm.hooks;

import dev.fiki.forgehax.asm.events.movement.PushedByBlockEvent;
import net.minecraft.client.entity.player.ClientPlayerEntity;

public class PushHooks {
  public static boolean onPushedByBlock(ClientPlayerEntity player) {
    return ForgeHaxHooks.EVENT_BUS.post(new PushedByBlockEvent(player));
  }
}
