package com.matt.forgehax.mods.services;

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

import java.util.Objects;

import static com.matt.forgehax.Helper.getWorld;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ScoreboardListenerService extends ServiceMod {
    public final Setting<Integer> wait = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("wait")
            .description("Time to wait after joining world")
            .defaultTo(5000)
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
        if(event.getPacket() instanceof SPacketPlayerListItem
                && getWorld() != null
                && System.currentTimeMillis() > waitTime) {
            final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
            packet.getEntries().stream()
                    .filter(Objects::nonNull)
                    .filter(data -> data.getProfile() != null)
                    .filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()))
                    .forEach(data -> {
                        final String name = data.getProfile().getName();
                        PlayerInfo info = PlayerInfoHelper.get(name);
                        if(info == null) {
                            new Thread(() -> {
                                PlayerInfo i = PlayerInfoHelper.lookup(name);
                                if(i != null) fireEvents(packet.getAction(), i, data.getProfile());
                            }).start();
                        } else fireEvents(packet.getAction(), info, data.getProfile());
                    });
        }
    }
}
