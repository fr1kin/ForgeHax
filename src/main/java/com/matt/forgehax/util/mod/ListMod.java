package com.matt.forgehax.util.mod;

import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;

import java.util.Comparator;
import java.util.List;

import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

public abstract class ListMod extends HudMod {
  protected final Setting<Boolean> showWatermark;
  protected final Setting<ListSorter> sortMode;

  protected enum ListSorter {
    ALPHABETICAL((o1, o2) -> 0), // mod list is already sorted alphabetically
    LENGTH(Comparator.<String>comparingInt(SurfaceHelper::getTextWidth).reversed());

    private final Comparator<String> comparator;

    public Comparator<String> getComparator() {
      return this.comparator;
    }

    ListSorter(Comparator<String> comparatorIn) {
      this.comparator = comparatorIn;
    }
  }

  public int watermarkOffsetX;
  public int watermarkOffsetY;
  public int alignOffsetY;
  public int align = alignment.get().ordinal();

  public ListMod(Category category, String modName, boolean defaultEnabled, String description) {
    super(category, modName, defaultEnabled, description);

    this.showWatermark =
        getCommandStub()
            .builders()
            .<Boolean>newSettingBuilder()
            .name("watermark")
            .description("Shows ForgeHax epic watermark")
            .defaultTo(true)
            .build();

    this.sortMode =
        getCommandStub()
            .builders()
            .<ListSorter>newSettingEnumBuilder()
            .name("sorting")
            .description("Sorting mode")
            .defaultTo(ListSorter.LENGTH)
            .build();
  }

  public String appendArrow(final String text) {
    return AlignHelper.getFlowDirX2(alignment.get()) == 1 ? ">" + text : text + "<";
  }

  public void listAlignmentAdjust() {
    watermarkOffsetX = 0;
    watermarkOffsetY = 0;
    alignOffsetY = 0;

    // adjust x offset
    if (alignment.get() == AlignHelper.Align.BOTTOMLEFT
        || alignment.get() == AlignHelper.Align.CENTERLEFT
        || alignment.get() == AlignHelper.Align.TOPLEFT) {
      watermarkOffsetX = 2;
    }

    // adjust y offset
    if (alignment.get() == AlignHelper.Align.TOPLEFT
        || alignment.get() == AlignHelper.Align.TOP
        || alignment.get() == AlignHelper.Align.TOPRIGHT) {
      if (showWatermark.get()) {
        alignOffsetY = 2;
      }
      watermarkOffsetY = 2;
    } else if (alignment.get() == AlignHelper.Align.BOTTOMLEFT
        || alignment.get() == AlignHelper.Align.BOTTOM
        || alignment.get() == AlignHelper.Align.BOTTOMRIGHT) {
      alignOffsetY = 1;
    } else {
      alignOffsetY = -5;
    }
  }

  public void printList(int align, List<String> text) {
    SurfaceHelper.drawTextAlign(text, getPosX(offsetX.get()), getPosY(offsetY.get()),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
  }

  /**
   * Needs ForgeHaxService.INSTANCE.drawWatermark(final int posX, final int posY, final int align) before the list in the child mod
   * Needs listAlignmentAdjust() before drawWatermark
   */
  public void printListWithWatermark(int align, List<String> text) {
    SurfaceHelper.drawTextAlign(text, getPosX(offsetX.get()),
        getPosY((showWatermark.get() ? getTextHeight(ForgeHaxService.INSTANCE.watermarkScale.get()) + alignOffsetY : offsetY.get() - alignOffsetY)),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
  }
}
