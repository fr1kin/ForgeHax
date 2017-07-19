package com.matt.forgehax.events;

import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class ChatMessageEvent extends Event {
    private final GameProfile profile;
    private final String message;
    private final boolean whispering;

    public ChatMessageEvent(GameProfile profile, String message, boolean whispering) {
        this.profile = profile;
        this.message = Strings.nullToEmpty(message);
        this.whispering = whispering;
    }

    @Nullable
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
