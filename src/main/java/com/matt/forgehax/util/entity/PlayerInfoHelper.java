package com.matt.forgehax.util.entity;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.matt.forgehax.Globals;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfoHelper implements Globals {
    private static final int THREAD_COUNT = 4;
    public static final int MAX_NAME_LENGTH = 16;

    private static final ListeningExecutorService EXECUTOR_SERVICE = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(THREAD_COUNT));

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

    /**
     * Will either use already cached player info or start a new service to lookup the player info
     * @param name
     * @param callback
     */
    public static boolean invokeEfficiently(final String name, final FutureCallback<PlayerInfo> callback) {
        PlayerInfo info = get(name);
        if(info == null) {
            ListenableFuture<PlayerInfo> future = EXECUTOR_SERVICE.submit(() -> PlayerInfoHelper.register(name));
            Futures.addCallback(future, callback);
            return false; // using thread
        } else {
            callback.onSuccess(info);
            return true; // using cache
        }

    }

    public static boolean invokeEfficiently(final UUID uuid, final FutureCallback<PlayerInfo> callback) {
        PlayerInfo info = get(uuid);
        if(info == null) {
            ListenableFuture<PlayerInfo> future = EXECUTOR_SERVICE.submit(() -> PlayerInfoHelper.register(uuid));
            Futures.addCallback(future, callback);
            return false; // using thread
        } else {
            callback.onSuccess(info);
            return true; // using cache
        }

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
