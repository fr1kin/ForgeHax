package com.matt.forgehax.mods.infooverlay;

import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

@RegisterMod
public class TickRate extends ToggleMod {

  public TickRate() {
    super(Category.GUI, "TickRate", true, "Shows the server tick-rate data");
  }

  public enum tickRateModes {
    TICKRATE,
    TPS
  }

  public final Setting<tickRateModes> tickRateMode =
    getCommandStub()
      .builders()
      .<tickRateModes>newSettingEnumBuilder()
      .name("mode")
      .description("Modes for tick-rate")
      .defaultTo(tickRateModes.TICKRATE)
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

  /*@Override
  public boolean isVisible() { return false; }*/

  public String getInfoDisplayText() {
    TickRateService.TickRateData data = TickRateService.getTickData();
    StringBuilder builderTickRate = new StringBuilder();

    if (tickRateMode.get() == tickRateModes.TICKRATE) {
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
            builderTickRate.append(String.format("%.2f", point.getAverage()));
            builderTickRate.append(" (");
            builderTickRate.append(at);
            builderTickRate.append(")");
            if ((i - 1) != 0) builderTickRate.append(", ");
          }
        }
      }
    } else if (tickRateMode.get() == tickRateModes.TPS) {
      TickRateService.TickRateData.CalculationData point = data.getPoint();
      builderTickRate.append(String.format("TPS: %.2f", point.getAverage()));
    }

    // Last tick function.
    if (showLag.get()) {
      float lastTickMs = TickRateService.getInstance().getLastTimeDiff();

      if (lastTickMs < 1000) {

        // Adds 0.0s to the StringBuilder.
        builderTickRate.append(", 0.0s");
      } else {

        // Adds lag time in seconds since last tick.
        builderTickRate.append(String.format(", %01.1fs", ((float) (lastTickMs - 1000)) / 1000));
      }
    }

    return builderTickRate.toString();
  }
}
