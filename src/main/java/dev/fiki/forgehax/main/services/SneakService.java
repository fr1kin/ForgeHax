package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.PacketHelper;
import dev.fiki.forgehax.api.entity.LocalPlayerUtils;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
@RequiredArgsConstructor
public class SneakService extends ServiceMod {
  @FieldMapping(parentClass = CEntityActionPacket.class, value = "entityID")
  private final ReflectionField<Integer> CEntityActionPacket_entityID;
  
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
    LocalPlayerUtils.setSneaking(sneaking);
  }
  
  public boolean isSneakingClient() {
    return sneakingClient;
  }
  
  public boolean isSneakingServer() {
    return sneakingServer;
  }
  
  @SubscribeEvent
  public void onPacketSend(PacketInboundEvent event) {
    if (event.getPacket() instanceof CEntityActionPacket) {
      CEntityActionPacket packet = (CEntityActionPacket) event.getPacket();
      int id = CEntityActionPacket_entityID.get(packet);
      if (Common.getLocalPlayer().getEntityId() == id
          && (packet.getAction() == Action.RELEASE_SHIFT_KEY || packet.getAction() == Action.PRESS_SHIFT_KEY)
          && !PacketHelper.isIgnored(packet)) {
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
