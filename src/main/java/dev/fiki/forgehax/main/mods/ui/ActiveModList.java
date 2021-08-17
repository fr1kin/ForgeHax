package dev.fiki.forgehax.main.mods.ui;

import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.EnumSetting;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.math.AlignHelper;
import dev.fiki.forgehax.api.mod.AbstractMod;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.HudMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.services.TickRateService;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ActiveMods",
    description = "Shows a list of all active mods",
    category = Category.UI,
    flags = EnumFlag.HIDDEN,
    enabled = true
)
@RequiredArgsConstructor
public class ActiveModList extends HudMod {
  private final BooleanSetting tps_meter = newBooleanSetting()
      .name("tps-meter")
      .description("Shows the server tps")
      .defaultTo(true)
      .build();

  private final BooleanSetting debug = newBooleanSetting()
      .name("debug")
      .description("Disables debug text on mods that have it")
      .defaultTo(false)
      .build();

  private final LongSetting timeoutDisplay = newLongSetting()
      .name("timeout-display")
      .description("Time required to elapse until lag in ms is shown")
      .defaultTo(1000L)
      .min(1L)
      .build();

  private final BooleanSetting showLag = newBooleanSetting()
      .name("show-lag")
      .description("Shows lag time since last tick")
      .defaultTo(true)
      .build();

  private final EnumSetting<SortMode> sortMode = newEnumSetting(SortMode.class)
      .name("sorting")
      .description("Sorting mode")
      .defaultTo(SortMode.ALPHABETICAL)
      .build();

  private final TickRateService tickRateService;

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

  private String generateTickRateText() {
    String text = "Tick-rate: ";
    if (!tickRateService.isEmpty()) {
      text += String.format("%1.2f", tickRateService.getRealtimeTickrate());

      if (showLag.getValue()) {
        text += " : ";
        TickRateService.TickrateTimer current = tickRateService.getCurrentTimer();
        if (current != null
            && current.getTimeElapsed() > timeoutDisplay.getValue()) {
          text += String.format("%01.1fs", (float) (current.getTimeElapsed() - 1000L) / 1000.f);
        } else {
          text += "0.0s";
        }
      }
    } else {
      text += "<unavailable>";
    }

    return text;
  }

  @SubscribeListener
  public void onRenderScreen(RenderPlaneEvent.Back event) {
    int align = alignment.getValue().ordinal();

    List<String> text = new ArrayList<>();

    if (tps_meter.getValue()) {
      text.add(generateTickRateText());
    }

    if (getDisplayScreen() instanceof ChatScreen || getGameSettings().renderDebug) {
      long enabledMods = getModManager().getMods()
          .filter(AbstractMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .count();
      text.add(enabledMods + " mods enabled");
    } else {
      getModManager().getMods()
          .filter(AbstractMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .map(mod -> debug.getValue() ? mod.getDebugDisplayText() : mod.getDisplayText())
          .sorted(sortMode.getValue().getComparator())
          .forEach(name -> text.add(AlignHelper.getFlowDirX2(align) == 1 ? ">" + name : name + "<"));
    }

    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(0),
        Colors.WHITE.toBuffer(), scale.getValue(), true, align);
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
