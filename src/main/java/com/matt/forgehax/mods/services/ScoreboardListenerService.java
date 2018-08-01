package com.matt.forgehax.mods.services;

import com.google.common.util.concurrent.FutureCallback;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.PlayerInfo;
import com.matt.forgehax.util.entity.PlayerInfoHelper;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import joptsimple.internal.Strings;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.matt.forgehax.Helper.getLog;
import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ScoreboardListenerService extends ServiceMod {
    private final Setting<Integer> wait = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("wait")
            .description("Time to wait after joining world")
            .defaultTo(5000)
            .build();
    private final Setting<Integer> retries = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("retries")
            .description("Number of times to attempt retries on failure")
            .defaultTo(3)
            .build();

    public ScoreboardListenerService() {
        super("ScoreboardListenerService", "Listens for player joining and leaving");
    }

    private void fireEvents(SPacketPlayerListItem.Action action, PlayerInfo info, GameProfile profile) {
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

    private long waitTime = 0;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        waitTime = System.currentTimeMillis() + wait.get();
    }

    @SubscribeEvent
    public void onScoreboardEvent(PacketEvent.Incoming.Pre event) {
        if(event.getPacket() instanceof SPacketPlayerListItem) {
            final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
            final boolean shouldEventFire = System.currentTimeMillis() > waitTime;
            packet.getEntries().stream()
                    .filter(Objects::nonNull)
                    .filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()))
                    .forEach(data -> {
                        final String name = data.getProfile().getName();
                        final AtomicInteger retries = new AtomicInteger(0);
                        final int retryCount = this.retries.get();
                        PlayerInfoHelper.invokeEfficiently(name, new FutureCallback<PlayerInfo>() {
                            @Override
                            public void onSuccess(@Nullable PlayerInfo result) {
                                if(result != null && shouldEventFire) fireEvents(packet.getAction(), result, data.getProfile());
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                if(retries.getAndIncrement() <= retryCount) {
                                    getLog().warn("Failed to lookup " + name + ", retrying (" + retries.get() + ")...");
                                    PlayerInfoHelper.invokeEfficiently(name, this);
                                } else {
                                    t.printStackTrace();
                                    PlayerInfoHelper.invokeEfficiently(name, true, this);
                                }
                            }
                        });
                    });
        }
    }
}
