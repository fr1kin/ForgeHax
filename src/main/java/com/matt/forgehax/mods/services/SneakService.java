package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.asm.reflection.FastReflection.Fields.CPacketEntityAction_entityID;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.PacketHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
  public void onPacketSend(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof CPacketEntityAction) {
      CPacketEntityAction packet = (CPacketEntityAction) event.getPacket();
      int id = CPacketEntityAction_entityID.get(packet);
      if (getLocalPlayer().getEntityId() == id
          && (packet.getAction() == Action.START_SNEAKING
              || packet.getAction() == Action.STOP_SNEAKING)
          && !PacketHelper.isIgnored(packet)) {
        sneakingClient = packet.getAction() == Action.START_SNEAKING;
        if (isSuppressing()) event.setCanceled(true);
        else sneakingServer = sneakingClient;
      }
    }
  }
}
