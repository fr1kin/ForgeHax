package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.PacketHelper;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class NoFallMod extends ToggleMod {
  
  public NoFallMod() {
    super(Category.PLAYER, "NoFall", false, "Prevents fall damage from being taken");
  }
  
  private float lastFallDistance = 0;
  
  @SubscribeEvent
  public void onPacketSend(PacketInboundEvent event) {
    if (event.getPacket() instanceof CPlayerPacket
        && !(event.getPacket() instanceof CPlayerPacket.RotationPacket)
        && !PacketHelper.isIgnored(event.getPacket())) {
      CPlayerPacket packetPlayer = (CPlayerPacket) event.getPacket();
      if (FastReflection.Fields.CPacketPlayer_onGround.get(packetPlayer) && lastFallDistance >= 4) {
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
        Globals.sendNetworkPacket(packet);
        Globals.sendNetworkPacket(reposition);
        lastFallDistance = 0;
      } else {
        lastFallDistance = Globals.getLocalPlayer().fallDistance;
      }
    }
  }
}
