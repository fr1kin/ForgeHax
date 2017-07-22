package com.matt.forgehax.util.entity;

import com.google.common.collect.Maps;
import com.matt.forgehax.Globals;

import java.util.Map;
import java.util.UUID;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfoHelper implements Globals {
    public static final int MAX_NAME_LENGTH = 16;

    private static final Map<String, PlayerInfo> NAME_TO_INFO = Maps.newConcurrentMap();
    private static final Map<UUID, PlayerInfo> UUID_TO_INFO = Maps.newConcurrentMap();

    private static PlayerInfo register(String name) {
        if(name.length() > MAX_NAME_LENGTH) return null;
        PlayerInfo info = new PlayerInfo(name);
        NAME_TO_INFO.put(info.getName().toLowerCase(), info);
        UUID_TO_INFO.put(info.getId(), info);
        return info;
    }
    private static PlayerInfo register(UUID uuid) {
        PlayerInfo info = new PlayerInfo(uuid);
        NAME_TO_INFO.put(info.getName().toLowerCase(), info);
        UUID_TO_INFO.put(info.getId(), info);
        return info;
    }

    public static PlayerInfo get(String name) {
        return NAME_TO_INFO.get(name.toLowerCase());
    }
    public static PlayerInfo get(UUID uuid) {
        return UUID_TO_INFO.get(uuid);
    }

    public static PlayerInfo lookup(String name) {
        PlayerInfo info = get(name);
        if(info == null)
            return register(name);
        else
            return info;
    }
    public static PlayerInfo lookup(UUID uuid) {
        PlayerInfo info = get(uuid);
        if(info == null)
            return register(uuid);
        else
            return info;
    }

    public static UUID getIdFromString(String uuid) {
        if(uuid.contains("-"))
            return UUID.fromString(uuid);
        else
            return UUID.fromString(uuid.replaceFirst (
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
            ));
    }

    public static String getIdNoHyphens(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }
}
