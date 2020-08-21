package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "NoFall",
    description = "Prevents fall damage from being taken",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class NoFallMod extends ToggleMod {
  @FieldMapping(parentClass = CPlayerPacket.class, value = "onGround")
  private final ReflectionField<Boolean> CPacketPlayer_onGround;

  private float lastFallDistance = 0;

  @SubscribeEvent
  public void onPacketSend(PacketInboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket
        && !(event.getPacket() instanceof CPlayerPacket.RotationPacket)
        && !PacketHelper.isIgnored(event.getPacket())) {
      CPlayerPacket packetPlayer = (CPlayerPacket) event.getPacket();
      if (CPacketPlayer_onGround.get(packetPlayer) && lastFallDistance >= 4) {
        CPlayerPacket packet =
            new CPlayerPacket.PositionRotationPacket(
                ((CPlayerPacket) event.getPacket()).getX(0),
                1337 + ((CPlayerPacket) event.getPacket()).getY(0),
                ((CPlayerPacket) event.getPacket()).getZ(0),
                ((CPlayerPacket) event.getPacket()).getYaw(0),
                ((CPlayerPacket) event.getPacket()).getPitch(0),
                true);
        CPlayerPacket reposition =
            new CPlayerPacket.PositionRotationPacket(
                ((CPlayerPacket) event.getPacket()).getX(0),
                ((CPlayerPacket) event.getPacket()).getY(0),
                ((CPlayerPacket) event.getPacket()).getZ(0),
                ((CPlayerPacket) event.getPacket()).getYaw(0),
                ((CPlayerPacket) event.getPacket()).getPitch(0),
                true);
        PacketHelper.ignore(packet);
        PacketHelper.ignore(reposition);
        Common.sendNetworkPacket(packet);
        Common.sendNetworkPacket(reposition);
        lastFallDistance = 0;
      } else {
        lastFallDistance = Common.getLocalPlayer().fallDistance;
      }
    }
  }
}
