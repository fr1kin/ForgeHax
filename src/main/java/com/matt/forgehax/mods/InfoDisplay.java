package com.matt.forgehax.mods;

import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ListMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
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
public class InfoDisplay extends ListMod {
  public InfoDisplay() {
    super(Category.GUI, "InfoDisplay", true, "Shows various useful infos");
  }

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.TOPLEFT;
  }

  @Override
  protected int getDefaultOffsetX() {
    return 1;
  }

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

  @Override
  public boolean isVisible() {
    return false;
  } // default false

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();

    listAlignmentAdjust();

    // Prints the watermark
    if (showWatermark.get()) {
      ForgeHaxService.INSTANCE.drawWatermark(getPosX(watermarkOffsetX), getPosY(watermarkOffsetY), align);
    }

    // Prints all the "InfoDisplayElement" mods
    getModManager()
        .getMods()
        .stream()
        .filter(BaseMod::isEnabled)
        .filter(BaseMod::isInfoDisplayElement)
        .map(BaseMod::getInfoDisplayText)
        .sorted(sortMode.get().getComparator())
        .map(super::appendArrow)
        .forEach(text::add);

    // Prints on screen
    printListWithWatermark(align, text);
  }
}
