package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.entity.LocalPlayerUtils;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

@RegisterMod(
    name = "NoRotate",
    description = "Prevent server from setting client viewangles",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class NoRotate extends ToggleMod {
  @FieldMapping(parentClass = SPlayerPositionLookPacket.class, value = "yaw")
  private final ReflectionField<Float> SPlayerPositionLookPacket_yaw;

  @FieldMapping(parentClass = SPlayerPositionLookPacket.class, value = "pitch")
  private final ReflectionField<Float> SPlayerPositionLookPacket_pitch;

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      if (Common.getLocalPlayer() != null) {
        Angle angle = LocalPlayerUtils.getViewAngles();

        packet.getFlags().removeAll(Arrays.asList(Flags.X_ROT, Flags.Y_ROT));

        SPlayerPositionLookPacket_yaw.set(packet, angle.getYaw());
        SPlayerPositionLookPacket_pitch.set(packet, angle.getPitch());
      }
    }
  }
}
