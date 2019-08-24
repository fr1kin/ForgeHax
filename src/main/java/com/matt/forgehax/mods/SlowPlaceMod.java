package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection.Fields;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.matt.forgehax.Helper.getModManager;
import static java.util.Objects.nonNull;

@RegisterMod
public class SlowPlaceMod extends ToggleMod {
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
          .defaultTo(1f)
          .build();

  private int lastDelay;
  private TimerMod timerMod;

  public SlowPlaceMod() {
    super(Category.PLAYER, "SlowPlace", false, "Prevents you from placing a million blocks every click with Timer on");
  }

  private int getRightClickDelay() {
    return Fields.Minecraft_rightClickDelayTimer.get(MC);
  }

  private void setRightClickDelay(int newDelay) {
    Fields.Minecraft_rightClickDelayTimer.set(MC, newDelay);
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

    if (singlePlace.get()) {
      setRightClickDelay(Math.max(currentDelay, 2));
    } else if (lastDelay <= 1 && currentDelay > 0) {
      float factor = this.factor.get() * (timerSync.get() && nonNull(timerMod) && timerMod.isEnabled() ? timerMod.factor.get() : 1);

      setRightClickDelay(MathHelper.floor((currentDelay + 1) * factor - 1));
    }

    lastDelay = currentDelay;
  }
}
