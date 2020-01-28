package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class AntiHunger extends ToggleMod {
  
  public AntiHunger() {
    super(Category.PLAYER, "AntiHunger", false, "Don't use hunger for travelling");
  }

  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPlayerPacket) {
      CPlayerPacket packet = event.getPacket();
      if (getLocalPlayer().fallDistance <= 0.0F && !getPlayerController().getIsHittingBlock()) {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, false);
      } else {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, true);
      }
    }
    
    if (event.getPacket() instanceof CEntityActionPacket) {
      CEntityActionPacket packet = event.getPacket();
      if (packet.getAction() == CEntityActionPacket.Action.START_SPRINTING
          || packet.getAction() == CEntityActionPacket.Action.STOP_SPRINTING) {
        event.setCanceled(true);
      }
    }
  }
}

