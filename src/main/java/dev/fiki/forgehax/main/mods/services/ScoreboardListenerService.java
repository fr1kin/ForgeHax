package dev.fiki.forgehax.main.mods.services;

import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.events.ConnectToServerEvent;
import dev.fiki.forgehax.main.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.main.events.PlayerConnectEvent;
import dev.fiki.forgehax.main.util.SimpleTimer;
import dev.fiki.forgehax.main.util.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.main.util.entity.PlayerInfoHelper;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ScoreboardListenerService extends ServiceMod {

  private final IntegerSetting wait = newIntegerSetting()
      .name("wait")
      .description("Time to wait after joining world")
      .defaultTo(5000)
      .build();

  private final SimpleTimer timer = new SimpleTimer();

  private boolean ignore = false;

  public ScoreboardListenerService() {
    super("ScoreboardListenerService");
  }

  @SubscribeEvent
  public void onClientConnect(ConnectToServerEvent event) {
    ignore = false;
  }

  @SubscribeEvent
  public void onClientDisconnect(DisconnectFromServerEvent event) {
    ignore = false;
  }

  @SubscribeEvent
  public void onPacketIn(PacketInboundEvent event) {
    if (ignore && timer.isStarted() && timer.hasTimeElapsed(wait.getValue())) {
      ignore = false;
    }

    if (!ignore && event.getPacket() instanceof SCustomPayloadPlayPacket) {
      ignore = true;
      timer.start();
    } else if (ignore && event.getPacket() instanceof SChunkDataPacket) {
      ignore = false;
      timer.reset();
    }
  }

  @SubscribeEvent
  public void onScoreboardEvent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerListItemPacket) {
      final SPlayerListItemPacket packet = (SPlayerListItemPacket) event.getPacket();
      switch (packet.getAction()) {
        case ADD_PLAYER:
          // provided a profile name and uuid
          for(SPlayerListItemPacket.AddPlayerData data : packet.getEntries()) {
            GameProfile profile = data.getProfile();
            PlayerInfoHelper.getOrCreate(profile)
                .thenApply(info -> info.setConnected(true))
                .thenAccept(info -> MinecraftForge.EVENT_BUS.post(
                    new PlayerConnectEvent.Join(info, info.toGameProfile())));
          }
          break;
        case REMOVE_PLAYER:
          // only provided the uuid
          for(SPlayerListItemPacket.AddPlayerData data : packet.getEntries()) {
            GameProfile profile = data.getProfile();
            PlayerInfoHelper.getOrCreateByUuid(profile.getId())
                .thenApply(info -> info.setConnected(false))
                .thenAccept(info -> MinecraftForge.EVENT_BUS.post(
                    new PlayerConnectEvent.Leave(info, info.toGameProfile())));
          }
          break;
      }
    }
  }
}
