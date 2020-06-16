package com.matt.forgehax.util.mod;

import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.math.AlignHelper;

import java.util.Comparator;
import java.util.List;

import static com.matt.forgehax.util.draw.SurfaceHelper.getTextHeight;

public abstract class WatermarkListMod extends ListMod {

  protected final Setting<Boolean> showWatermark;
  protected final Setting<Integer> watermarkOffsetY;

  protected abstract int getDefaultWatermarkOffsetY();
  protected abstract boolean watermarkDefault();

  public WatermarkListMod(Category category, String modName, boolean defaultEnabled, String description) {
    super(category, modName, defaultEnabled, description);

    this.showWatermark =
        getCommandStub()
            .builders()
            .<Boolean>newSettingBuilder()
            .name("watermark")
            .description("Shows epic ForgeHax watermark")
            .defaultTo(watermarkDefault())
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

  /**
   * Needs ForgeHaxService.INSTANCE.drawWatermark(final int posX, final int posY, final int align) before the list in the child mod
   */
  public void printListWithWatermark(List<String> text, int align) {
    SurfaceHelper.drawTextAlign(text, getPosX(0),
        getPosY(showWatermark.get() ? getTextHeight(ForgeHaxService.INSTANCE.watermarkScale.get()) : 0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
  }
}
