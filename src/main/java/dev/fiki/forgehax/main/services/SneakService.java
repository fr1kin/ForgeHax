package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

@RegisterMod
@RequiredArgsConstructor
@ExtensionMethod({LocalPlayerEx.class})
public class SneakService extends ServiceMod {
  @MapField(parentClass = CEntityActionPacket.class, value = "id")
  private final ReflectionField<Integer> CEntityActionPacket_id;

  private boolean suppressing = false;
  private boolean sneakingClient = false;
  private boolean sneakingServer = false;

  public boolean isSuppressing() {
    return suppressing;
  }

  public void setSuppressing(boolean suppressing) {
    this.suppressing = suppressing;
  }

  public void setSneaking(boolean sneaking) {
    getLocalPlayer().setCrouchSneaking(sneaking);
  }

  public boolean isSneakingClient() {
    return sneakingClient;
  }

  public boolean isSneakingServer() {
    return sneakingServer;
  }

  @SubscribeListener
  public void onPacketSend(PacketInboundEvent event) {
    if (event.getPacket() instanceof CEntityActionPacket) {
      CEntityActionPacket packet = (CEntityActionPacket) event.getPacket();
      int id = CEntityActionPacket_id.get(packet);
      if (getLocalPlayer().getId() == id
          && (packet.getAction() == Action.RELEASE_SHIFT_KEY || packet.getAction() == Action.PRESS_SHIFT_KEY)) {
        sneakingClient = packet.getAction() == Action.PRESS_SHIFT_KEY;
        if (isSuppressing()) {
          event.setCanceled(true);
        } else {
          sneakingServer = sneakingClient;
        }
      }
    }
  }
}
