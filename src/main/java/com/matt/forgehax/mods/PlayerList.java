package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.util.math.VectorUtils.distance;
import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.mods.services.FriendService;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ListMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

/**
 * Bruh no Tonio did this I just copied InfoDisplay.java to start
 */

@RegisterMod
public class PlayerList extends ListMod {

  public PlayerList() {
    super(Category.GUI, "PlayerList", false, "Displays nearby players and some stats");
  }

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.TOPLEFT;
  }

  @Override
  protected int getDefaultOffsetX() { return 100; }

  @Override
  protected int getDefaultOffsetY() { return 1; }

  @Override
  protected double getDefaultScale() { return 0.5d; }

  @Override
  public boolean isInfoDisplayElement() { return false; }

  private int count = 0;

  @Override
  public String getDisplayText() {
    return (getModName() + " [" + TextFormatting.DARK_AQUA + count + TextFormatting.WHITE + "]");
  }

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!MC.gameSettings.showDebugInfo) {
      int align = alignment.get().ordinal();
	    List<String> text = new ArrayList<>();

      EntityPlayer player = getLocalPlayer();

      getWorld()
        .loadedEntityList
        .stream()
        .filter(EntityUtils::isPlayer)
        .filter(
          entity ->
            !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
		    .map(entity -> (EntityPlayer) entity)
        .map(entity -> (getDistanceColor(distance(entity.posX, entity.posY, entity.posZ, player.posX, player.posY, player.posZ)) +
                        getNameColor(entity) + " [" +
                        getHPColor(entity.getHealth()) + TextFormatting.GRAY + "] " +
                        above_below(getLocalPlayer().posY, entity.posY)))
        .sorted(sortMode.get().getComparator())
        .forEach(line -> text.add(line));

      count = text.size();

      // Prints on screen
      SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
    }
  }

  private static String getHPColor(float hp) {
    if (hp > 19.9F) return TextFormatting.DARK_GREEN + String.format("%.0f", hp);
    if (hp > 17F) return TextFormatting.GREEN + String.format("%.0f", hp);
    if (hp > 12F) return TextFormatting.YELLOW + String.format("%.0f", hp);
    if (hp > 8F) return TextFormatting.GOLD + String.format("%.0f", hp);
    if (hp > 5F) return TextFormatting.RED + String.format("%.1f", hp);
    if (hp > 2F) return TextFormatting.DARK_RED + String.format("%.1f", hp);
    return TextFormatting.DARK_GRAY + String.format("%.1f", hp);
  }

  private static String above_below(double pos1, double pos2) {
    if (pos1 > pos2) return TextFormatting.GOLD + "++ ";
    if (pos1 < pos2) return TextFormatting.DARK_GRAY + "-- ";
    return TextFormatting.GRAY + "== ";
  }

  private static String getDistanceColor(double distance) {
    if (distance > 30D) return TextFormatting.DARK_AQUA + String.format("%.1fm ", distance);
    if (distance > 10D) return TextFormatting.AQUA + String.format("%.1fm ", distance);
    return TextFormatting.WHITE + String.format("%.1fm ", distance);
  }

  private String getNameColor(EntityPlayer entity) {
    if (getModManager().get(FriendService.class).get().isFriend(entity.getName()))
      return TextFormatting.LIGHT_PURPLE + entity.getName() + TextFormatting.GRAY;
    return TextFormatting.GRAY + entity.getName();
  }
}
