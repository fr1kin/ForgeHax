package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.mods.managers.PositionRotationManager;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.math.Angle;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Arrays;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketPlayerPosLook.EnumFlags;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class NoRotate extends ToggleMod {
  
  public NoRotate() {
    super(Category.MISC, "NoRotate", false,
        "Prevent server from setting client viewangles");
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketPlayerPosLook) {
      SPacketPlayerPosLook packet = event.getPacket();
      if (getLocalPlayer() != null) {
        Angle angle = LocalPlayerUtils.getViewAngles();
        
        packet.getFlags().removeAll(Arrays.asList(EnumFlags.X_ROT, EnumFlags.Y_ROT));
        
        FastReflection.Fields.SPacketPlayer_yaw.set(packet, angle.getYaw());
        FastReflection.Fields.SPacketPlayer_pitch.set(packet, angle.getPitch());
      }
    }
  }
}
