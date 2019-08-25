package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getModManager;
import static java.util.Objects.nonNull;

/**
 * Created on 9/4/2016 by fr1kin
 */
@RegisterMod
public class FastPlaceMod extends ToggleMod {
  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> singlePlace =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("single")
          .description("only let you place a single block per click rather than simply slowing it down")
          .defaultTo(false)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Boolean> timerSync =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("timer-sync")
          .description("place at the same speed even if timer is on")
          .defaultTo(true)
          .build();

  @SuppressWarnings("WeakerAccess")
  public final Setting<Float> factor =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("factor")
          .description("how long the delay should be, 1 for default")
          .min(0f)
          .defaultTo(0f)
          .build();

  private int lastDelay;
  private TimerMod timerMod;

  public FastPlaceMod() {
    super(Category.PLAYER, "FastPlace", false, "Place blocks faster or slower");
  }

  private int getRightClickDelay() {
    return FastReflection.Fields.Minecraft_rightClickDelayTimer.get(MC);
  }

  private void setRightClickDelay(int newDelay) {
    FastReflection.Fields.Minecraft_rightClickDelayTimer.set(MC, newDelay);
  }

  @Override
  public void onEnabled() {
    super.onEnabled();

    lastDelay = getRightClickDelay();
    timerMod = getModManager().get(TimerMod.class).orElse(null);
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    int currentDelay = getRightClickDelay();
    float factor = this.factor.get();

    if (singlePlace.get()) {
      setRightClickDelay(Math.max(currentDelay, 2));
    } else if (factor == 0) {
      setRightClickDelay(0);
    } else if (lastDelay <= 1 && currentDelay > 0) {
      float multiplier = factor * (timerSync.get() && nonNull(timerMod) && timerMod.isEnabled() ? timerMod.factor.get() : 1);

      setRightClickDelay(Math.max(MathHelper.floor((currentDelay + 1) * multiplier - 1), 0));
    }

    lastDelay = currentDelay;
  }
}
