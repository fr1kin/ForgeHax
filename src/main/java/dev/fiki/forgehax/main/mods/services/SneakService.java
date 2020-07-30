package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.PacketHelper;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class SneakService extends ServiceMod {
  
  private static SneakService instance;
  
  public static SneakService getInstance() {
    return instance;
  }
  
  private boolean suppressing = false;
  private boolean sneakingClient = false;
  private boolean sneakingServer = false;
  
  public SneakService() {
    super("SneakService");
    instance = this;
  }
  
  public boolean isSuppressing() {
    return suppressing;
  }
  
  public void setSuppressing(boolean suppressing) {
    this.suppressing = suppressing;
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
      int id = FastReflection.Fields.CEntityActionPacket_entityID.get(packet);
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
