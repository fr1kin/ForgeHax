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
  protected final Setting<Integer> watermarkOffsetY;

  protected abstract int getDefaultWatermarkOffsetY();
  protected abstract boolean watermarkDefault();

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

  public ListMod(Category category, String modName, boolean defaultEnabled, String description) {
    super(category, modName, defaultEnabled, description);

    this.showWatermark =
        getCommandStub()
            .builders()
            .<Boolean>newSettingBuilder()
            .name("watermark")
            .description("Shows epic ForgeHax watermark")
            .defaultTo(watermarkDefault())
            .build();

    this.sortMode =
        getCommandStub()
            .builders()
            .<ListSorter>newSettingEnumBuilder()
            .name("sorting")
            .description("Sorting mode")
            .defaultTo(ListSorter.LENGTH)
            .build();

    this.watermarkOffsetY =
        getCommandStub()
            .builders()
            .<Integer>newSettingBuilder()
            .name("watermark-y-offset")
            .description("Watermark Y offset")
            .defaultTo(getDefaultWatermarkOffsetY())
            .min(1)
            .build();
  }

  public String appendArrow(final String text) {
    return AlignHelper.getFlowDirX2(alignment.get()) == 1 ? ">" + text : text + "<";
  }

  public void printList(int align, List<String> text) {
    SurfaceHelper.drawTextAlign(text, getPosX(offsetX.get()), getPosY(offsetY.get()),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
  }

  /**
   * Needs ForgeHaxService.INSTANCE.drawWatermark(final int posX, final int posY, final int align) before the list in the child mod
   */
  public void printListWithWatermark(List<String> text, int align) {
    SurfaceHelper.drawTextAlign(text, getPosX(0),
        getPosY(showWatermark.get() ? getTextHeight(ForgeHaxService.INSTANCE.watermarkScale.get()) : 0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
  }
}
