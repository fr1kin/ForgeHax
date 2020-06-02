package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class Latency extends ToggleMod {

  public Latency() {
    super(Category.GUI, "Latency", true, "Shows your ping");
  }

  int ping;

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  @Override
  public boolean notInList() {
	return true;
  }

  public String getInfoDisplayText() {
    return "Ping: " + String.format("%s ms", latencyCalc());
  }

  private int latencyCalc(){
    if (MC.getConnection() == null) {
      return 1;
    } else if (MC.player == null) {
      return -1;
    } else {
      try {

        // Returns the player's ping
        return ping = MC.getConnection().getPlayerInfo(MC.player.getUniqueID()).getResponseTime();
      } catch (NullPointerException ignored) {
      }
      return -1;
    }
  }
}
