package com.matt.forgehax.mods.services;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.PlayerConnectEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

/**
 * Created on 7/18/2017 by fr1kin
 */
@RegisterMod
public class ScoreboardListenerService extends ServiceMod {
    public ScoreboardListenerService() {
        super("ScoreboardListenerService", "Listens for player joining and leaving");
    }

    @SubscribeEvent
    public void onScoreboardEvent(PacketEvent.Incoming.Pre event) {
        if(event.getPacket() instanceof SPacketPlayerListItem) {
            final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
            packet.getEntries().stream()
                    .filter(Objects::nonNull)
                    .filter(data -> data.getProfile() != null)
                    .forEach(data -> {
                        switch (packet.getAction()) {
                            case ADD_PLAYER:
                            {
                                MinecraftForge.EVENT_BUS.post(new PlayerConnectEvent.Join(data.getProfile()));
                                break;
                            }
                            case REMOVE_PLAYER:
                            {
                                MinecraftForge.EVENT_BUS.post(new PlayerConnectEvent.Leave(data.getProfile()));
                                break;
                            }
                        }
                    });
        }
    }
}
