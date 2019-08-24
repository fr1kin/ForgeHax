package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.Globals;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerUtils implements Globals {
  
  /**
   * Use EntityUtils::isLocalPlayer
   */
  @Deprecated
  public static boolean isLocalPlayer(Entity player) {
    EntityPlayer localPlayer = getLocalPlayer();
    return localPlayer != null && localPlayer.equals(player);
  }

  @Deprecated
  public static boolean isFriend(EntityPlayer player) {
    return false;
  }
}
