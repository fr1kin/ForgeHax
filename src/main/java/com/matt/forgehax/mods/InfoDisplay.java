package com.matt.forgehax.mods;

import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.HudMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

import static com.matt.forgehax.Helper.getModManager;
import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

/**
 * Created by OverFloyd
 * may 2020
 */
@RegisterMod
public class InfoDisplay extends HudMod {

  public InfoDisplay() {
    super(Category.GUI, "InfoDisplay", false, "Shows various useful infos");
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
  protected int getDefaultOffsetX() { return 0; }

  @Override
  protected int getDefaultOffsetY() {
    return 1;
  }

  @Override
  protected double getDefaultScale() {
    return 1d;
  }

  @Override
  public boolean isInfoDisplayElement() {
    return false;
  }

  int posY;

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    if (!MC.gameSettings.showDebugInfo) {
      int align = alignment.get().ordinal();
      List<String> text = new ArrayList<>();

      // Prints all the "InfoDisplayElement" mods
      getModManager()
        .getMods()
        .stream()
        .filter(BaseMod::isEnabled)
        .filter(BaseMod::isInfoDisplayElement)
        .map(BaseMod::getInfoDisplayText)
        .sorted(sortMode.get().getComparator())
        .forEach(name -> text.add(AlignHelper.getFlowDirX2(align) == 1 ? "> " + name : name + " <"));

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
