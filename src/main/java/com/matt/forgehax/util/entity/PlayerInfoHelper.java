package com.matt.forgehax.util.entity;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.Immutables;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import joptsimple.internal.Strings;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfoHelper implements Globals {

  private static final int THREAD_COUNT = 1;
  public static final int MAX_NAME_LENGTH = 16;

  private static final ListeningExecutorService EXECUTOR_SERVICE =
    MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Math.max(THREAD_COUNT, 1)));

  private static final Map<String, PlayerInfo> NAME_TO_INFO = Maps.newConcurrentMap();
  private static final Map<UUID, PlayerInfo> UUID_TO_INFO = Maps.newConcurrentMap();

  static {
    // shut threads down
    Runtime.getRuntime()
      .addShutdownHook(
        new Thread(
          () -> {
            EXECUTOR_SERVICE.shutdown();
            while (!EXECUTOR_SERVICE.isShutdown()) {
              try {
                EXECUTOR_SERVICE.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
              } catch (InterruptedException e) {
              }
            }
          }));
  }

  private static PlayerInfo register(String name) throws IOException {
    if (Strings.isNullOrEmpty(name) || name.length() > MAX_NAME_LENGTH) {
      return null;
    }
    PlayerInfo info = new PlayerInfo(name);
    NAME_TO_INFO.put(info.getName().toLowerCase(), info);
    UUID_TO_INFO.put(info.getId(), info);
    return info;
  }

  private static PlayerInfo register(UUID uuid) throws IOException {
    PlayerInfo info = new PlayerInfo(uuid);
    NAME_TO_INFO.put(info.getName().toLowerCase(), info);
    UUID_TO_INFO.put(info.getId(), info);
    return info;
  }

  private static PlayerInfo offlineUser(String name) {
    if (name.length() > MAX_NAME_LENGTH) {
      return null;
    }
    return new PlayerInfo(name, true);
  }

  public static PlayerInfo get(String name) {
    return Strings.isNullOrEmpty(name) ? null : NAME_TO_INFO.get(name.toLowerCase());
  }

  public static PlayerInfo get(UUID uuid) {
    return uuid == null ? null : UUID_TO_INFO.get(uuid);
  }

  public static List<PlayerInfo> getPlayers() {
    return Immutables.copyToList(UUID_TO_INFO.values());
  }

  public static List<PlayerInfo> getOnlinePlayers() {
    return MC.getConnection() == null
      ? Collections.emptyList()
      : MC.getConnection()
        .getPlayerInfoMap()
        .stream()
        .map(
          info -> {
            PlayerInfo pl = get(info.getGameProfile().getName());
            return pl == null ? offlineUser(info.getGameProfile().getName()) : pl;
          })
        .collect(Collectors.toList());
  }

  public static PlayerInfo lookup(String name) throws IOException {
    PlayerInfo info = get(name);
    if (info == null) {
      return register(name);
    } else {
      return info;
    }
  }

  public static PlayerInfo lookup(UUID uuid) throws IOException {
    PlayerInfo info = get(uuid);
    if (info == null) {
      return register(uuid);
    } else {
      return info;
    }
  }

  /**
   * Will either use already cached player info or start a new service to lookup the player info
   *
   * @return true if the player info is being looked up on a separate thread
   */
  @SuppressWarnings("Duplicates")
  public static boolean registerWithCallback(
    final String name, final FutureCallback<PlayerInfo> callback) {
    PlayerInfo info = get(name);
    if (info == null) {
      Futures.addCallback(EXECUTOR_SERVICE.submit(() -> PlayerInfoHelper.register(name)), callback);
      return true;
    } else {
      Futures.addCallback(Futures.immediateFuture(info), callback);
      return false;
    }
  }

  @SuppressWarnings("Duplicates")
  public static boolean registerWithCallback(
    final UUID uuid, final FutureCallback<PlayerInfo> callback) {
    PlayerInfo info = get(uuid);
    if (info == null) {
      Futures.addCallback(EXECUTOR_SERVICE.submit(() -> PlayerInfoHelper.register(uuid)), callback);
      return true;
    } else {
      Futures.addCallback(Futures.immediateFuture(info), callback);
      return false;
    }
  }

  public static boolean registerWithCallback(
    final UUID uuid, final String name, final FutureCallback<PlayerInfo> callback) {
    return registerWithCallback(
      uuid,
      new FutureCallback<PlayerInfo>() {
        @Override
        public void onSuccess(@Nullable PlayerInfo result) {
          callback.onSuccess(
            result); // uuid successfully found player data, call original onSuccess
        }
    
        @Override
        public void onFailure(Throwable t) {
          registerWithCallback(name, callback); // try name instead
        }
      });
  }

  public static boolean generateOfflineWithCallback(
    final String name, final FutureCallback<PlayerInfo> callback) {
    ListenableFuture<PlayerInfo> future =
      Futures.immediateFuture(PlayerInfoHelper.offlineUser(name));
    Futures.addCallback(future, callback);
    return false;
  }

  public static UUID getIdFromString(String uuid) {
    if (uuid.contains("-")) {
      return UUID.fromString(uuid);
    } else {
      return UUID.fromString(
        uuid.replaceFirst(
          "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
          "$1-$2-$3-$4-$5"));
    }
  }

  public static String getIdNoHyphens(UUID uuid) {
    return uuid.toString().replaceAll("-", "");
  }
}
