package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class ActiveModList extends ToggleMod {
  private final Setting<Boolean> tps_meter =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("tps-meter")
          .description("Shows the server tps")
          .defaultTo(true)
          .build();

  private final Setting<Boolean> debug =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("debug")
          .description("Disables debug text on mods that have it")
          .defaultTo(false)
          .build();

  private final Setting<Integer> factor =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("factor")
          .description("Splitting up the tick rate data")
          .defaultTo(25)
          .min(1)
          .max(100)
          .build();

  private final Setting<Boolean> showLag =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("showLag")
          .description("Shows lag time since last tick")
          .defaultTo(true)
          .build();

  private final Setting<SortMode> sortMode =
      getCommandStub()
          .builders()
          .<SortMode>newSettingEnumBuilder()
          .name("sorting")
          .description("Sorting mode")
          .defaultTo(SortMode.ALPHABETICAL)
          .build();

  public ActiveModList() {
    super(Category.RENDER, "ActiveMods", true, "Shows list of all active mods");
  }

  @Override
  public boolean isHidden() {
    return true;
  }

  private String generateTickRateText() {
    StringBuilder builder = new StringBuilder("Tick-rate: ");
    TickRateService.TickRateData data = TickRateService.getTickData();
    if (data.getSampleSize() <= 0) {
      builder.append("No tick data");
    } else {
      int factor = this.factor.get();
      int sections = data.getSampleSize() / factor;
      if ((sections * factor) < data.getSampleSize()) {
        TickRateService.TickRateData.CalculationData point = data.getPoint();
        builder.append(String.format("%.2f", point.getAverage()));
        builder.append(" (");
        builder.append(data.getSampleSize());
        builder.append(")");
        if (sections > 0) builder.append(", ");
      }
      if (sections > 0) {
        for (int i = sections; i > 0; i--) {
          int at = i * factor;
          TickRateService.TickRateData.CalculationData point = data.getPoint(at);
          builder.append(String.format("%.2f", point.getAverage()));
          builder.append(" (");
          builder.append(at);
          builder.append(")");
          if ((i - 1) != 0) builder.append(", ");
        }
      }
    }

    if (showLag.get()) {
      long lastTickMs = TickRateService.getInstance().getLastTimeDiff();

      if (lastTickMs < 1000) {
        builder.append(", 0.0s");
      } else {
        builder.append(String.format(", %01.1fs", ((float) (lastTickMs - 1000)) / 1000));
      }
    }

    return builder.toString();
  }

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    int posX = 1;
    final AtomicInteger posY = new AtomicInteger(1);
    if (tps_meter.get()) {
      SurfaceHelper.drawTextShadow(generateTickRateText(), posX, posY.get(), Utils.Colors.WHITE);
      posY.addAndGet(SurfaceHelper.getTextHeight() + 1);
    }
    getModManager()
        .getMods()
        .stream()
        .filter(BaseMod::isEnabled)
        .filter(mod -> !mod.isHidden())
        .map(mod -> debug.get() ? mod.getDebugDisplayText() : mod.getDisplayText())
        .sorted(sortMode.get().getComparator())
        .forEach(
            name -> {
              SurfaceHelper.drawTextShadow(">" + name, posX, posY.get(), Utils.Colors.WHITE);
              posY.addAndGet(SurfaceHelper.getTextHeight() + 1);
            });
    /*
    posY += (Render2DUtils.getTextHeight() + 1) * 2;
    Render2DUtils.drawTextShadow(String.format("Pitch: %.4f", MC.thePlayer.rotationPitch), posX, posY, Utils.toRGBA(255, 255, 255, 255));
    posY += Render2DUtils.getTextHeight() + 1;
    Render2DUtils.drawTextShadow(String.format("Yaw: %.4f", MC.thePlayer.rotationYaw), posX, posY, Utils.toRGBA(255, 255, 255, 255));*/
  }

  private enum SortMode {
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
