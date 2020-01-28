package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class NoFallMod extends ToggleMod {
  
  public NoFallMod() {
    super(Category.PLAYER, "NoFall", false, "Prevents fall damage from being taken");
  }
  
  private float lastFallDistance = 0;
  
  @SubscribeEvent
  public void onPacketSend(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPlayerPacket
        && !(event.getPacket() instanceof CPlayerPacket.RotationPacket)
        && !PacketHelper.isIgnored(event.getPacket())) {
      CPlayerPacket packetPlayer = event.getPacket();
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
        sendNetworkPacket(packet);
        sendNetworkPacket(reposition);
        lastFallDistance = 0;
      } else {
        lastFallDistance = getLocalPlayer().fallDistance;
      }
    }
  }
}
