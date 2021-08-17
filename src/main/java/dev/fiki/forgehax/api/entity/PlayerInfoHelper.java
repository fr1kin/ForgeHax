package dev.fiki.forgehax.api.entity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.main.Common;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.getPooledThreadExecutor;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfoHelper implements Common {
  public static final int MAX_NAME_LENGTH = 16;
  
  private static final Map<String, PlayerInfo> NAME_TO_INFO = Maps.newConcurrentMap();
  private static final Map<UUID, PlayerInfo> UUID_TO_INFO = Maps.newConcurrentMap();
  
  public static PlayerInfo register(String username, UUID uuid) {
    Objects.requireNonNull(username, "username is null");
    if (username.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException("Username is too long!");
    }

    final PlayerInfo info = new PlayerInfo(username, uuid);
    if(!username.isEmpty()) {
      NAME_TO_INFO.put(username.toLowerCase(), info);
    }
    UUID_TO_INFO.put(uuid, info);
    UUID_TO_INFO.put(info.getOfflineId(), info);
    return info;
  }

  public static PlayerInfo registerOnline(String username, UUID uuid) {
    return register(username, uuid);
  }

  public static PlayerInfo registerOffline(String username) {
    return register(username, PlayerEntity.createPlayerUUID(username));
  }

  public static PlayerInfo registerOffline(UUID uuid) {
    return register("", uuid);
  }
  
  public static PlayerInfo get(String name) {
    Objects.requireNonNull(name, "username is null");
    return NAME_TO_INFO.get(name.toLowerCase());
  }
  
  public static PlayerInfo get(UUID uuid) {
    Objects.requireNonNull(uuid, "uuid is null");
    return UUID_TO_INFO.get(uuid);
  }
  
  public static Collection<PlayerInfo> getPlayers() {
    return Collections.unmodifiableCollection(UUID_TO_INFO.values());
  }

  public static Collection<PlayerInfo> getOnlinePlayers() {
    return UUID_TO_INFO.values().stream()
        .filter(PlayerInfo::isConnected)
        .collect(Collectors.toList());
  }

  public static CompletableFuture<PlayerInfo> getOrCreate(String username, UUID uuid) {
    PlayerInfo info = get(uuid);
    if(info == null) {
      info = registerOnline(username, uuid);
    }
    return CompletableFuture.completedFuture(info)
        .exceptionally(ex -> PlayerInfoHelper.getOrCreateOffline(username)
            .getNow(null));
  }

  public static CompletableFuture<PlayerInfo> getOrCreate(GameProfile profile) {
    Objects.requireNonNull(profile, "GameProfile is null");
    Objects.requireNonNull(profile.getId(), "GameProfile.id is null");
    Objects.requireNonNull(profile.getName(), "GameProfile.name is null");
    return getOrCreate(profile.getName(), profile.getId());
  }

  public static CompletableFuture<PlayerInfo> getOrCreateOffline(String username) {
    PlayerInfo info = get(username);
    if(info == null) {
      info = registerOffline(username);
    }
    return CompletableFuture.completedFuture(info);
  }
  
  /**
   * Will either use already cached player info or start a new service to lookup the player info
   *
   * @return true if the player info is being looked up on a separate thread
   */
  public static CompletableFuture<PlayerInfo> getOrCreateByUsername(final String username) {
    PlayerInfo info = get(username);
    if (info == null) {
      return CompletableFuture.supplyAsync(() -> PlayerInfo.getUuidFromName(username), getPooledThreadExecutor())
          .exceptionally(ex -> PlayerEntity.createPlayerUUID(username))
          .thenApply(uuid -> registerOnline(username, uuid));
    }
    return CompletableFuture.completedFuture(info);
  }
  
  public static CompletableFuture<PlayerInfo> getOrCreateByUuid(final UUID uuid) {
    PlayerInfo info = get(uuid);
    if (info == null) {
      return CompletableFuture.supplyAsync(() -> PlayerInfo.getNameHistory(uuid), getPooledThreadExecutor())
          .thenApply(names -> {
            PlayerInfo pli = registerOnline(names.get(0).getName(), uuid);
            pli.setNames(names);
            return pli;
          })
          .exceptionally(ex -> registerOffline(uuid));
    }
    return CompletableFuture.completedFuture(info);
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
