package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AntiHunger extends ToggleMod {

  public AntiHunger() {
    super(Category.PLAYER, "AntiHunger", false, "Don't use hunger for travelling");
  }


  @SubscribeEvent
  public void onPacketSending(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketPlayer) {
      CPacketPlayer packet = (CPacketPlayer)event.getPacket();
      if (MC.player.fallDistance <= 0.0F && !MC.playerController.getIsHittingBlock()) {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, false);
      } else {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, true);
      }
    }

    if (event.getPacket() instanceof CPacketEntityAction) {
      CPacketEntityAction packet = (CPacketEntityAction)event.getPacket();
      if (packet.getAction() == CPacketEntityAction.Action.START_SPRINTING || packet.getAction() == CPacketEntityAction.Action.STOP_SPRINTING) {
        event.setCanceled(true);
      }
    }
  }
}

