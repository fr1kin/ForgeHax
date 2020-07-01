package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraft.util.text.TextFormatting;

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

  public String getInfoDisplayText() {
    int ping = latencyCalc();
    return ("Ping: " + getColorPing(ping) + ping + TextFormatting.WHITE + " ms");
  }

  private int latencyCalc() {
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

  private static String getColorPing(int ping) {
    if (ping > 1000) return TextFormatting.DARK_GRAY.toString();
    if (ping > 500) return TextFormatting.DARK_RED.toString();
    if (ping > 300) return TextFormatting.RED.toString();
    if (ping > 180) return TextFormatting.GOLD.toString();
    if (ping > 100) return TextFormatting.YELLOW.toString();
    if (ping > 70) return TextFormatting.GREEN.toString();
    if (ping > 40) return TextFormatting.DARK_GREEN.toString();
    return TextFormatting.DARK_AQUA.toString();
  }
}
