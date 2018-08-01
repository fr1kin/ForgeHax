package com.matt.forgehax.util.entity;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.Immutables;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfoHelper implements Globals {
    private static final int THREAD_COUNT = 1;
    public static final int MAX_NAME_LENGTH = 16;

    private static final ListeningExecutorService EXECUTOR_SERVICE = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Math.max(THREAD_COUNT, 1)));

    private static final Map<String, PlayerInfo> NAME_TO_INFO = Maps.newConcurrentMap();
    private static final Map<UUID, PlayerInfo> UUID_TO_INFO = Maps.newConcurrentMap();

    static {
        // shut threads down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            EXECUTOR_SERVICE.shutdown();
            while(!EXECUTOR_SERVICE.isShutdown()) try {
                EXECUTOR_SERVICE.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {}
        }));
    }

    private static PlayerInfo register(String name) throws IOException {
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

    private static PlayerInfo offlineUser(String name) {
        if(name.length() > MAX_NAME_LENGTH) return null;
        return new PlayerInfo(name, true);
    }

    public static PlayerInfo get(String name) {
        return NAME_TO_INFO.get(name.toLowerCase());
    }
    public static PlayerInfo get(UUID uuid) {
        return UUID_TO_INFO.get(uuid);
    }

    public static List<PlayerInfo> getPlayers() {
        return Immutables.copyToList(UUID_TO_INFO.values());
    }

    public static List<PlayerInfo> getOnlinePlayers() {
        return MC.getConnection() == null ? Collections.emptyList() : MC.getConnection().getPlayerInfoMap().stream()
                .map(info -> {
                    PlayerInfo pl = get(info.getGameProfile().getName());
                    return pl == null ? offlineUser(info.getGameProfile().getName()) : pl;
                })
                .collect(Collectors.toList());
    }

    public static PlayerInfo lookup(String name) throws IOException {
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

    /**
     * Will either use already cached player info or start a new service to lookup the player info
     * @param name
     * @param callback
     * @return true if the player info is being looked up on a separate thread
     */
    public static boolean invokeEfficiently(final String name, final boolean offline, final FutureCallback<PlayerInfo> callback) {
        PlayerInfo info = get(name);
        ListenableFuture<PlayerInfo> future;
        boolean threaded;
        if(info == null) {
            if(offline) {
                future = Futures.immediateFuture(PlayerInfoHelper.offlineUser(name));
                threaded = false;
            }
            else {
                future = EXECUTOR_SERVICE.submit(() -> PlayerInfoHelper.register(name));
                threaded = true;
            }
        } else {
            future = Futures.immediateFuture(info);
            threaded = false;
        }
        Futures.addCallback(future, callback);
        return threaded;
    }
    public static boolean invokeEfficiently(final String name, final FutureCallback<PlayerInfo> callback) {
        return invokeEfficiently(name, false, callback);
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
