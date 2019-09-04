package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLog;

import com.google.common.util.concurrent.FutureCallback;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.PlayerInfo;
import com.matt.forgehax.util.entity.PlayerInfoHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import joptsimple.internal.Strings;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

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

  private void fireEvents(
    SPacketPlayerListItem.Action action, PlayerInfo info, GameProfile profile) {
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
  public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
    ignore = false;
  }

  @SubscribeEvent
  public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    ignore = false;
  }

  @SubscribeEvent
  public void onPacketIn(PacketEvent.Incoming.Pre event) {
    if (ignore && timer.isStarted() && timer.hasTimeElapsed(wait.get())) {
      ignore = false;
    }

    if (!ignore && event.getPacket() instanceof SPacketCustomPayload) {
      ignore = true;
      timer.start();
    } else if (ignore && event.getPacket() instanceof SPacketChunkData) {
      ignore = false;
      timer.reset();
    }
  }

  @SubscribeEvent
  public void onScoreboardEvent(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketPlayerListItem) {
      final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
      if (!Action.ADD_PLAYER.equals(packet.getAction())
        && !Action.REMOVE_PLAYER.equals(packet.getAction())) {
        return;
      }

      packet
        .getEntries()
        .stream()
        .filter(Objects::nonNull)
        .filter(
          data ->
            !Strings.isNullOrEmpty(data.getProfile().getName())
              || data.getProfile().getId() != null)
        .forEach(
          data -> {
            final String name = data.getProfile().getName();
            final UUID id = data.getProfile().getId();
            final AtomicInteger retries = new AtomicInteger(this.retries.get());
            PlayerInfoHelper.registerWithCallback(
              id,
              name,
              new FutureCallback<PlayerInfo>() {
                @Override
                public void onSuccess(@Nullable PlayerInfo result) {
                  fireEvents(packet.getAction(), result, data.getProfile());
                }
          
                @Override
                public void onFailure(Throwable t) {
                  if (retries.getAndDecrement() > 0) {
                    getLog()
                      .warn(
                        "Failed to lookup "
                          + name
                          + "/"
                          + id.toString()
                          + ", retrying ("
                          + retries.get()
                          + ")...");
                    PlayerInfoHelper.registerWithCallback(
                      data.getProfile().getId(), name, this);
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
