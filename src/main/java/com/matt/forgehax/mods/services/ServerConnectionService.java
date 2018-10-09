package com.matt.forgehax.mods.services;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.ServerConnectionEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.login.server.SPacketLoginSuccess;

@RegisterMod
public class ServerConnectionService extends ServiceMod {

  public ServerConnectionService() {
    super("ServerConnectionService", "Fires server connect/disconnect events");
  }

  @Subscribe
  public void onPacketSent(PacketEvent.Outgoing.Pre event) {
    if (event.getPacket() instanceof SPacketLoginSuccess) {
      ForgeHax.EVENT_BUS.post(new ServerConnectionEvent.Connect());
    }
  }

}
