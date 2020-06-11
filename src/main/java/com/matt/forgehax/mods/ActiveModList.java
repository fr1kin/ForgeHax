package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getModManager;

import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.math.AlignHelper.Align;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ListMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class ActiveModList extends ListMod {
  private final Setting<Boolean> showDebugText =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("debug")
          .description("Enables debug text on mods that have it")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> showServiceMods =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("show-service-mod")
          .description("Shows service mods count when mod list is compressed")
          .defaultTo(false)
          .build();

  private final Setting<Boolean> condense =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("condense")
          .description("Condense ModList when chat is open")
          .defaultTo(false)
          .build();

  public ActiveModList() {
    super(Category.GUI, "ActiveMods", true, "Shows list of all active mods");
  }

  @Override
  public boolean isInfoDisplayElement() {
    return false;
  }

  @Override
  protected Align getDefaultAlignment() {
    return Align.BOTTOMRIGHT;
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

    if (condense.get() && (MC.currentScreen instanceof GuiChat || MC.gameSettings.showDebugInfo)) {

      // Total number of service mods
      long serviceMods = getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isHidden)
          .count();

      // Total number of mods in the client
      long totalMods = getModManager()
          .getMods()
          .stream()
          .filter(mod -> !mod.isHidden())
          .count();

      // Mods that are enabled
      long enabledMods = getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .count();

      text.add(enabledMods + "/" + totalMods + " mods enabled");
      if (showServiceMods.get()) {
        text.add(serviceMods + " service mods");
      }
    } else {
      getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isEnabled)
          .filter(mod -> !mod.isHidden())
          .filter(mod -> !mod.isInfoDisplayElement())
          .filter(BaseMod::isVisible) // prints only visible mods
          .map(mod -> showDebugText.get() ? mod.getDebugDisplayText() : mod.getDisplayText())
          .sorted(sortMode.get().getComparator())
          .map(super::appendArrow)
          .forEach(text::add);
    }

    // Prints on screen
    printListWithWatermark(align, text);
  }
}
