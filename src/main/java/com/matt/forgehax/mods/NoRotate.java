package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class NoRotate extends ToggleMod {
  public NoRotate() {
    super(Category.PLAYER, "NoRotate", false, "dont let server set pitch and yaw");
  }

  @SubscribeEvent
  public void onPacketRecieved(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = event.getPacket();
      if (MC.player != null)
        if (MC.player.rotationYaw != -180 && MC.player.rotationPitch != 0) {
          FastReflection.Fields.SPacketPlayer_yaw.set(packet, MC.player.rotationYaw);
          FastReflection.Fields.SPacketPlayer_pitch.set(packet, MC.player.rotationPitch);
        }
    }
  }
}
