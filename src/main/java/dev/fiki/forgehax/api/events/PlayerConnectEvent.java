package dev.fiki.forgehax.api.events;

import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.api.entity.PlayerInfo;
import dev.fiki.forgehax.api.event.Event;

import java.util.Objects;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class PlayerConnectEvent extends Event {
  
  private final PlayerInfo playerInfo;
  private final GameProfile profile;
  
  public PlayerConnectEvent(PlayerInfo playerInfo, GameProfile profile) {
    Objects.requireNonNull(profile);
    this.playerInfo = playerInfo;
    this.profile = profile;
  }
  
  public PlayerInfo getPlayerInfo() {
    return playerInfo;
  }
  
  public GameProfile getProfile() {
    return profile;
  }
  
  public static class Join extends PlayerConnectEvent {
    public Join(PlayerInfo playerInfo, GameProfile profile) {
      super(playerInfo, profile);
    }
  }
  
  public static class Leave extends PlayerConnectEvent {
    public Leave(PlayerInfo playerInfo, GameProfile profile) {
      super(playerInfo, profile);
    }
  }
}
