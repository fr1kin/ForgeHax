package com.matt.forgehax.mods;

import java.util.stream.Collectors;

import static com.matt.forgehax.Helper.getWorld;
import static com.matt.forgehax.Helper.getLocalPlayer;

import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

/**
 * Created by OverFloyd
 * may 2020
 */
@RegisterMod
public class PlayerList extends HudMod {

  public PlayerList() {
    super(Category.GUI, "PlayerList", false, "Displays nearby players and some stats");
  }

  private final Setting<SortMode> sortMode =
    getCommandStub()
      .builders()
      .<SortMode>newSettingEnumBuilder()
      .name("sorting")
      .description("alphabetical or length")
      .defaultTo(SortMode.LENGTH)
      .build();

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

  int posY;

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!MC.gameSettings.showDebugInfo) {
      int align = alignment.get().ordinal();
	  List<String> text = new ArrayList<>();

      // Prints all the "InfoDisplayElement" mods
      getWorld()
        .loadedEntityList
        .stream()
        .filter(EntityUtils::isPlayer)
        .filter(
          entity ->
            !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
		.map(entity -> (EntityPlayer) entity)
        .map(entity -> entity.getDisplayName().getUnformattedText() + String.format(" [%.1f HP]", entity.getHealth()))
        .sorted(sortMode.get().getComparator())
        .forEach(name -> text.add(AlignHelper.getFlowDirX2(align) == 1 ? "> " + name : name + " <"));

	  
      // Shift up when chat is open && alignment is at bottom
      if (alignment.get().toString().startsWith("BOTTOM") && MC.currentScreen instanceof GuiChat) {
        posY = getPosY(offsetY.get() + 15);
      } else {
        posY = getPosY(offsetY.get() + 0);
      }

      // Prints on screen
      SurfaceHelper.drawTextAlign(text, getPosX(offsetX.get()), posY,
        Colors.WHITE.toBuffer(), scale.get(), true, align);
    }
  }

  public enum SortMode {
    ALPHABETICAL((o1, o2) -> 0), // mod list is already sorted alphabetically
    LENGTH(Comparator.<String>comparingInt(SurfaceHelper::getTextWidth).reversed());

    private final Comparator<String> comparator;

    public Comparator<String> getComparator() {
      return this.comparator;
    }

    SortMode(Comparator<String> comparatorIn) {
      this.comparator = comparatorIn;
    }
  }
}
