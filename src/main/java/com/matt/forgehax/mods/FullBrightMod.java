package com.matt.forgehax.mods;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Objects;

/**
 * Updated by OverFloyd
 * may 2020
 */
@RegisterMod
public class FullBrightMod extends ToggleMod {

  private final Setting<Float> defaultGamma =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("gamma")
          .description("Default gamma to revert to")
          .defaultTo(MC.gameSettings.gammaSetting)
          .min(0.1F)
          .max(16F)
          .build();

  public enum Mode {
    GAMMA,
    POTION
  }

  public final Setting<Mode> brightnessMode =
      getCommandStub()
          .builders()
          .<Mode>newSettingEnumBuilder()
          .name("mode")
          .description("Mode for FullBright")
          .defaultTo(Mode.GAMMA)
          .build();

  public FullBrightMod() {
    super(Category.WORLD, "FullBright", false, "Makes everything render with maximum brightness");
  }

  @Override
  public void onEnabled() {
    if (brightnessMode.get() == Mode.GAMMA) {
      MC.gameSettings.gammaSetting = 16F;
    } else if (brightnessMode.get() == Mode.POTION) {
      setEffect();
    }
  }

  @Override
  public void onDisabled() {
    if (brightnessMode.get() == Mode.POTION) {
      MC.player.removeActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(16)));
    }
    MC.gameSettings.gammaSetting = defaultGamma.get();
  }

  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if(event.phase != TickEvent.Phase.START || MC.player == null) {
      return;
    }

    switch (brightnessMode.get()) {
      case GAMMA: {
        MC.gameSettings.gammaSetting = 16F;
        MC.player.removeActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(16)));
        break;
      }
      case POTION: {
        setEffect();
        break;
      }
      default: {
        MC.gameSettings.gammaSetting = defaultGamma.get();
        MC.player.removeActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(16)));
      }
    }
  }

  private void setEffect() {
    final PotionEffect potionEffect = new PotionEffect(Objects.requireNonNull(Potion.getPotionById(16)), 32767, 0,
        false, false); // le 32k potion effect lol
    potionEffect.setPotionDurationMax(true);
    Objects.requireNonNull(MC.getConnection()).handleEntityEffect(new SPacketEntityEffect(MC.player.getEntityId(), potionEffect));

    // Gamma set to 100
    MC.gameSettings.gammaSetting = 1;
  }
}
