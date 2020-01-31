package dev.fiki.forgehax.main.mods.services;

import com.google.common.util.concurrent.FutureCallback;
import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.events.ConnectToServerEvent;
import dev.fiki.forgehax.main.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.main.events.PlayerConnectEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.PlayerInfo;
import dev.fiki.forgehax.main.util.entity.PlayerInfoHelper;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.SimpleTimer;
import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

import joptsimple.internal.Strings;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ScoreboardListenerService extends ServiceMod {
  
  private final Setting<Integer> wait =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("wait")
          .description("Time to wait after joining world")
          .defaultTo(5000)
          .build();
  private final Setting<Integer> retries =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("retries")
          .description("Number of times to attempt retries on failure")
          .defaultTo(1)
          .build();
  
  private final SimpleTimer timer = new SimpleTimer();
  
  private boolean ignore = false;
  
  public ScoreboardListenerService() {
    super("ScoreboardListenerService", "Listens for player joining and leaving");
  }
  
  private void fireEvents(Action action, PlayerInfo info, GameProfile profile) {
    if (ignore || info == null) {
      return;
    }
    switch (action) {
      case ADD_PLAYER: {
        MinecraftForge.EVENT_BUS.post(new PlayerConnectEvent.Join(info, profile));
        break;
      }
      case REMOVE_PLAYER: {
        MinecraftForge.EVENT_BUS.post(new PlayerConnectEvent.Leave(info, profile));
        break;
      }
    }
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
    if (ignore && timer.isStarted() && timer.hasTimeElapsed(wait.get())) {
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
      if (!Action.ADD_PLAYER.equals(packet.getAction()) && !Action.REMOVE_PLAYER.equals(packet.getAction())) {
        return;
      }
      
      packet.getEntries().stream()
          .filter(Objects::nonNull)
          .filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null)
          .forEach(data -> {
            final String name = data.getProfile().getName();
            final UUID id = data.getProfile().getId();
            final AtomicInteger retries = new AtomicInteger(this.retries.get());
            PlayerInfoHelper.registerWithCallback(id, name, new FutureCallback<PlayerInfo>() {
              @Override
              public void onSuccess(@Nullable PlayerInfo result) {
                fireEvents(packet.getAction(), result, data.getProfile());
              }

              @Override
              public void onFailure(Throwable t) {
                if (retries.getAndDecrement() > 0) {
                  Globals.getLogger().warn("Failed to lookup {}/{}, retrying ({})...",
                      name, id.toString(), retries.get());

                  PlayerInfoHelper.registerWithCallback(data.getProfile().getId(), name, this);
                } else {
                  t.printStackTrace();
                  PlayerInfoHelper.generateOfflineWithCallback(name, this);
                }
              }
            });
          });
    }
  }
}
