package dev.fiki.forgehax.asm.hooks;

import dev.fiki.forgehax.asm.events.movement.PushedByBlockEvent;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.common.MinecraftForge;

public class PushHooks {
  public static boolean onPushedByBlock(ClientPlayerEntity player) {
    return MinecraftForge.EVENT_BUS.post(new PushedByBlockEvent(player));
  }
}
