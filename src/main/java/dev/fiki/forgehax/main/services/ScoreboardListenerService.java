package dev.fiki.forgehax.main.services;

import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.api.SimpleTimer;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.entity.PlayerInfoHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.ConnectToServerEvent;
import dev.fiki.forgehax.api.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.api.events.PlayerConnectEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;

import static dev.fiki.forgehax.main.Common.getEventBus;

@RegisterMod
public class ScoreboardListenerService extends ServiceMod {
  private final IntegerSetting wait = newIntegerSetting()
      .name("wait")
      .description("Time to wait after joining world")
      .defaultTo(5000)
      .build();

  private final SimpleTimer timer = new SimpleTimer();

  private boolean ignore = false;

  @SubscribeListener
  public void onClientConnect(ConnectToServerEvent event) {
    ignore = false;
  }

  @SubscribeListener
  public void onClientDisconnect(DisconnectFromServerEvent event) {
    ignore = false;
  }

  @SubscribeListener
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

  @SubscribeListener
  public void onScoreboardEvent(PacketInboundEvent event) {
    if (event.getPacket() instanceof SPlayerListItemPacket) {
      final SPlayerListItemPacket packet = (SPlayerListItemPacket) event.getPacket();
      switch (packet.getAction()) {
        case ADD_PLAYER:
          // provided a profile name and uuid
          for (SPlayerListItemPacket.AddPlayerData data : packet.getEntries()) {
            GameProfile profile = data.getProfile();
            PlayerInfoHelper.getOrCreate(profile)
                .thenApply(info -> info.setConnected(true))
                .thenAccept(info -> getEventBus().post(new PlayerConnectEvent.Join(info, info.toGameProfile())));
          }
          break;
        case REMOVE_PLAYER:
          // only provided the uuid
          for (SPlayerListItemPacket.AddPlayerData data : packet.getEntries()) {
            GameProfile profile = data.getProfile();
            PlayerInfoHelper.getOrCreateByUuid(profile.getId())
                .thenApply(info -> info.setConnected(false))
                .thenAccept(info -> getEventBus().post(new PlayerConnectEvent.Leave(info, info.toGameProfile())));
          }
          break;
      }
    }
  }
}
