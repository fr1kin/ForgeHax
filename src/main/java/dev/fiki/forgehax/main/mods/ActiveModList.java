package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.math.AlignHelper;
import dev.fiki.forgehax.main.util.mod.BaseMod;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.HudMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.mods.services.TickRateService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ActiveModList extends HudMod {
  
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
  
  @Override
  protected AlignHelper.Align getDefaultAlignment() { return AlignHelper.Align.TOPLEFT; }
  @Override
  protected int getDefaultOffsetX() { return 1; }
  @Override
  protected int getDefaultOffsetY() { return 1; }
  @Override
  protected double getDefaultScale() { return 1d; }
  
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
    int align = alignment.get().ordinal();
    
    List<String> text = new ArrayList<>();
    
    if (tps_meter.get()) {
      text.add(generateTickRateText());
    }
    
    if (Common.getDisplayScreen() instanceof ChatScreen || Common.MC.gameSettings.showDebugInfo) {
      long enabledMods = Common.getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .count();
      text.add(enabledMods + " mods enabled");
    } else {
      Common.getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .map(mod -> debug.get() ? mod.getDebugDisplayText() : mod.getDisplayText())
          .sorted(sortMode.get().getComparator())
          .forEach(name -> text.add(AlignHelper.getFlowDirX2(align) == 1 ? ">" + name : name + "<"));
    }
  
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.get(), true, align);
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
