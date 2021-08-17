package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.math.Angle;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod(
    name = "NoRotate",
    description = "Prevent server from setting client viewangles",
    category = Category.PLAYER
)
@RequiredArgsConstructor
@ExtensionMethod({LocalPlayerEx.class})
public class NoRotate extends ToggleMod {
  @MapField(parentClass = SPlayerPositionLookPacket.class, value = "yRot")
  private final ReflectionField<Float> SPlayerPositionLookPacket_yaw;

  @MapField(parentClass = SPlayerPositionLookPacket.class, value = "xRot")
  private final ReflectionField<Float> SPlayerPositionLookPacket_pitch;

  @SubscribeEvent
  public void onPacketReceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
      SPlayerPositionLookPacket packet = (SPlayerPositionLookPacket) event.getPacket();
      if (getLocalPlayer() != null) {
        Angle angle = getLocalPlayer().getViewAngles();

        packet.getRelativeArguments().removeAll(Arrays.asList(Flags.X_ROT, Flags.Y_ROT));

        SPlayerPositionLookPacket_yaw.set(packet, angle.getYaw());
        SPlayerPositionLookPacket_pitch.set(packet, angle.getPitch());
      }
    }
  }
}
