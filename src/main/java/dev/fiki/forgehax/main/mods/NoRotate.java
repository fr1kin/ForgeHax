package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.entity.LocalPlayerUtils;
import dev.fiki.forgehax.main.util.math.Angle;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

@RegisterMod
public class NoRotate extends ToggleMod {

  public NoRotate() {
    super(Category.PLAYER, "NoRotate", false,
        "Prevent server from setting client viewangles");
  }

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      if (Common.getLocalPlayer() != null) {
        Angle angle = LocalPlayerUtils.getViewAngles();

        packet.getFlags().removeAll(Arrays.asList(Flags.X_ROT, Flags.Y_ROT));

        FastReflection.Fields.SPlayerPositionLookPacket_yaw.set(packet, angle.getYaw());
        FastReflection.Fields.SPlayerPositionLookPacket_pitch.set(packet, angle.getPitch());
      }
    }
  }
}
