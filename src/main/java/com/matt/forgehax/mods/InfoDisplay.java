package com.matt.forgehax.mods;

import com.matt.forgehax.mods.infodisplay.EffectsList;
import com.matt.forgehax.mods.services.ForgeHaxService;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.EffectUtils;
import com.matt.forgehax.util.math.AlignHelper;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ListMod;
import com.matt.forgehax.util.mod.WatermarkListMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.potion.PotionEffect;
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
public class InfoDisplay extends WatermarkListMod {

  private final Setting<Boolean> condense =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("condense")
          .description("Condense ModList when chat is open.")
          .defaultTo(true)
          .build();

  public InfoDisplay() {
    super(Category.GUI, "InfoDisplay", true, "Shows various useful infos.");
  }

  @Override
  protected AlignHelper.Align getDefaultAlignment() {
    return AlignHelper.Align.TOPLEFT;
  }

  @Override
  protected int getDefaultOffsetX() {
    return 2;
  }

  @Override
  protected int getDefaultOffsetY() {
    return 1;
  }

  @Override
  protected int getDefaultWatermarkOffsetY() {
    return 1;
  }

  @Override
  protected double getDefaultScale() {
    return 1;
  }

  @Override
  public boolean watermarkDefault() {
    return true;
  }

  @Override
  public boolean isInfoDisplayElement() {
    return false;
  }

  @SubscribeEvent
  public void onRenderScreen(RenderGameOverlayEvent.Text event) {
    List<String> text = new ArrayList<>();
    boolean isEffectsListEnabled = getModManager().get(EffectsList.class).get().isEnabled();
    boolean showEffectsList = getModManager().get(EffectsList.class).get().showList.get();

    if (condense.get() && (MC.currentScreen instanceof GuiChat || MC.gameSettings.showDebugInfo)) {
      // Total number of InfoDisplay mods in the client
      long totalInfoMods = getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isInfoDisplayElement)
          .count();

      // Mods that are enabled
      long enabledInfoMods = getModManager()
          .getMods()
          .stream()
          .filter(BaseMod::isEnabled)
          .filter(BaseMod::isInfoDisplayElement)
          .count();

      text.add(enabledInfoMods + "/" + totalInfoMods + " InfoDisplay mods enabled");
    } else {
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
    }

    // Prints the watermark
    if (showWatermark.get()) {
      ForgeHaxService.INSTANCE.drawWatermark(getPosX(0), getPosY(watermarkOffsetY.get()), alignment.get().ordinal());
    }

    int yShift = showWatermark.get() ? getTextHeight(ForgeHaxService.INSTANCE.watermarkScale.get()) : 0;
    int yCount = 0;

    // Prints Info mods on screen
    SurfaceHelper.drawTextAlign(text, getPosX(0), getPosY(yShift),
        Colors.WHITE.toBuffer(), scale.get(), true, alignment.get().ordinal());

    /**
     * Effects list printing
     * by OverFloyd & Tonio_Cartonio
     */
    if (isEffectsListEnabled && showEffectsList) {
      final List<PotionEffect> effectList = new ArrayList<>(MC.player.getActivePotionEffects());

      for (PotionEffect potionEffect : effectList) {
        if (potionEffect == null) {
          continue;
        }

        // Prints EffectsList on screen
        SurfaceHelper.drawTextAlign(EffectUtils.getNameDurationString(potionEffect),
            getPosX(0), getPosY((int) ((text.size() + yCount) * (scale.get() * 10) + yShift)),
            potionEffect.getPotion().getLiquidColor(), scale.get(), true, alignment.get().ordinal());
        yCount++;
      }
    }
  }
}
