package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.main.util.events.ConnectToServerEvent;
import dev.fiki.forgehax.main.util.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ServerConnectionService extends ServiceMod {
  @SubscribeEvent
  public void onLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
    MinecraftForge.EVENT_BUS.post(new ConnectToServerEvent());
  }

  @SubscribeEvent
  public void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
    MinecraftForge.EVENT_BUS.post(new DisconnectFromServerEvent());
  }
}
