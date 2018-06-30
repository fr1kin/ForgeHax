package com.matt.forgehax.asm.events;


import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Babbaj on 2/17/2018.
 */
public class PlayerListAddEvent extends Event {

    public final NetworkPlayerInfo info;

    public PlayerListAddEvent(NetworkPlayerInfo infoIn) {
        this.info = infoIn;
    }
}
