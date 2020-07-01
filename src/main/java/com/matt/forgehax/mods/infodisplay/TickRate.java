package com.matt.forgehax.mods.infodisplay;

import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraft.util.text.TextFormatting;

@RegisterMod
public class TickRate extends ToggleMod {

  public TickRate() {
    super(Category.GUI, "TickRate", true, "Shows the server tick-rate data");
  }

  public final Setting<Boolean> tpsMode =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("tps")
          .description("Shows server TPS instaed of tick-rate")
          .defaultTo(false)
          .build();

  private final Setting<Integer> factor =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("factor")
          .description("Splitting up the tick rate data")
          .defaultTo(25)
          .min(25)
          .max(100)
          .build();

  private final Setting<Boolean> showLag =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("lag")
          .description("Shows lag time since last valid tick")
          .defaultTo(true)
          .build();

  @Override
  public boolean isInfoDisplayElement() {
    return true;
  }

  public String getInfoDisplayText() {
    TickRateService.TickRateData data = TickRateService.getTickData();
    StringBuilder builderTickRate = new StringBuilder();

    if (!tpsMode.get()) {
      builderTickRate.append("Tick-rate: ");

      if (data.getSampleSize() <= 0) {
        builderTickRate.append("No tick data");
      } else {
        int factor = this.factor.get();
        int sections = data.getSampleSize() / factor;

        if ((sections * factor) < data.getSampleSize()) {
          TickRateService.TickRateData.CalculationData point = data.getPoint();
          builderTickRate.append(String.format("%.2f", point.getAverage()));
          builderTickRate.append(" (");
          builderTickRate.append(data.getSampleSize());
          builderTickRate.append(")");

          if (sections > 0) builderTickRate.append(", ");
        }

        if (sections > 0) {
          for (int i = sections; i > 0; i--) {
            int at = i * factor;
            TickRateService.TickRateData.CalculationData point = data.getPoint(at);
            builderTickRate.append(String.format(
              getColorTPS(point.getAverage()) + "%.2f" + TextFormatting.WHITE, point.getAverage()));
            builderTickRate.append(" (");
            builderTickRate.append(at);
            builderTickRate.append(")");
            if ((i - 1) != 0) builderTickRate.append(", ");
          }
        }
      }
    } else {
      TickRateService.TickRateData.CalculationData point = data.getPoint();
      builderTickRate.append(String.format(
            "TPS: " + getColorTPS(point.getAverage()) + "%.2f" + TextFormatting.WHITE, point.getAverage()));
    }

    // Last tick function.
    if (showLag.get()) {
      float lastTickMs = TickRateService.getInstance().getLastTimeDiff();

      if (lastTickMs < 1000) {

        // Adds 0.0s to the StringBuilder.
        builderTickRate.append(TextFormatting.DARK_GRAY + " 0.0s" + TextFormatting.WHITE);
      } else {

        // Adds lag time in seconds since last tick.
        builderTickRate.append(String.format(" %01.1fs", ((float) (lastTickMs - 1000)) / 1000));
      }
    }

    return builderTickRate.toString();
  }

  private static String getColorTPS(double tps) {
    if (tps > 19D) return TextFormatting.DARK_GREEN.toString();
    if (tps > 16D) return TextFormatting.GREEN.toString();
    if (tps > 12) return TextFormatting.YELLOW.toString();
    if (tps > 8) return TextFormatting.GOLD.toString();
    if (tps > 5) return TextFormatting.RED.toString();
    if (tps > 2) return TextFormatting.DARK_RED.toString();
    return TextFormatting.DARK_GRAY.toString();
  }
}
