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

import java.util.*;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

/**
 * Created by OverFloyd
 * may 2020
 */
@RegisterMod
public class EntityList extends HudMod {

  public EntityList() {
    super(Category.GUI, "EntityList", false, "Displays a list of all rendered entities");
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
      List<String> entityList = new ArrayList<>();
	  List<String> text = new ArrayList<>();

      // Prints all the "InfoDisplayElement" mods
      getWorld()
        .loadedEntityList
        .stream()
        // .filter(EntityUtils::isLiving)
        .filter(
          entity ->
            !Objects.equals(getLocalPlayer(), entity) && !EntityUtils.isFakeLocalPlayer(entity))
        // .filter(EntityUtils::isAlive)
        .filter(EntityUtils::isValidEntity)
        .map(entity -> entity.getDisplayName().getUnformattedText())
        .sorted(sortMode.get().getComparator())
        .forEach(name -> entityList.add(name));
	  
	  String buf = "";
	  int num = 0;
	  for (String element : entityList.stream().distinct().collect(Collectors.toList())) {
		buf = String.format("%s", element);
		num = Collections.frequency(entityList, element);
		if (num > 1) buf += String.format(" (x%d)", num);
		text.add(AlignHelper.getFlowDirX2(align) == 1 ? "> " + buf : buf + " <");
	  }


    // Prints on screen
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
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
