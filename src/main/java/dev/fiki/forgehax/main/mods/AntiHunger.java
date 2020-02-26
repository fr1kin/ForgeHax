package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getPlayerController;

@RegisterMod
public class AntiHunger extends ToggleMod {

  public AntiHunger() {
    super(Category.PLAYER, "AntiHunger", false, "Don't use hunger for travelling");
  }

  @SubscribeEvent
  public void onPacketSending(PacketOutboundEvent event) {
    if(getLocalPlayer() == null || getLocalPlayer().isElytraFlying()) {
      // this will break elytra flying
      return;
    }

    if (event.getPacket() instanceof CPlayerPacket) {
      CPlayerPacket packet = (CPlayerPacket) event.getPacket();
      if ((getLocalPlayer().fallDistance <= 0.0F)
          && !getPlayerController().getIsHittingBlock()) {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, false);
      } else {
        FastReflection.Fields.CPacketPlayer_onGround.set(packet, true);
      }
    }

    if (event.getPacket() instanceof CEntityActionPacket) {
      CEntityActionPacket packet = (CEntityActionPacket) event.getPacket();
      if (packet.getAction() == CEntityActionPacket.Action.START_SPRINTING
          || packet.getAction() == CEntityActionPacket.Action.STOP_SPRINTING) {
        event.setCanceled(true);
      }
    }
  }
}

