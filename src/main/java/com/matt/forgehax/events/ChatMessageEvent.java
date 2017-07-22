package com.matt.forgehax.events;

import com.google.common.base.Strings;
import com.matt.forgehax.util.entity.PlayerInfo;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class ChatMessageEvent extends Event {
    private final PlayerInfo playerInfo;
    private final GameProfile profile;
    private final String message;
    private final boolean whispering;

    public ChatMessageEvent(PlayerInfo playerInfo, GameProfile profile, String message, boolean whispering) {
        this.playerInfo = playerInfo;
        this.profile = profile;
        this.message = Strings.nullToEmpty(message);
        this.whispering = whispering;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public String getMessage() {
        return message;
    }

    public boolean isWhispering() {
        return whispering;
    }
}
