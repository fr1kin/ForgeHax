package com.matt.forgehax.events;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Objects;
import java.util.UUID;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class PlayerConnectEvent extends Event {
    private final UUID uuid;
    private final GameProfile profile;

    public PlayerConnectEvent(GameProfile profile) {
        Objects.requireNonNull(profile);
        this.profile = profile;
        this.uuid = profile.getId();
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public static class Join extends PlayerConnectEvent {
        public Join(GameProfile profile) {
            super(profile);
        }
    }

    public static class Leave extends PlayerConnectEvent {
        public Leave(GameProfile profile) {
            super(profile);
        }
    }
}
