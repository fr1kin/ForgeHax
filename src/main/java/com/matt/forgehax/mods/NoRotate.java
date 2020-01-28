package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class NoRotate extends ToggleMod {
  
  public NoRotate() {
    super(Category.PLAYER, "NoRotate", false,
        "Prevent server from setting client viewangles");
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = event.getPacket();
      if (getLocalPlayer() != null) {
        Angle angle = LocalPlayerUtils.getViewAngles();
        
        packet.getFlags().removeAll(Arrays.asList(Flags.X_ROT, Flags.Y_ROT));
        
        FastReflection.Fields.SPacketPlayer_yaw.set(packet, angle.getYaw());
        FastReflection.Fields.SPacketPlayer_pitch.set(packet, angle.getPitch());
      }
    }
  }
}
