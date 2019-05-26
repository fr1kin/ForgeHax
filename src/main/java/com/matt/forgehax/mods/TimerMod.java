package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.Timer;

/** Created by Babbaj on 1/24/2018. */
@RegisterMod
public class TimerMod extends ToggleMod {

  public TimerMod() {
    super(Category.MISC, "Timer", false, "Speed up game time");
  }

  public final Setting<Float> factor =
      getCommandStub()
          .builders()
          .<Float>newSettingBuilder()
          .name("speed")
          .description("how fast to make the game run")
          .defaultTo(1f)
          .min(0f)
          .success(__ -> {
              if (this.isEnabled()) updateTimer();
          })
          .build();

  private final float DEFAULT_SPEED = 1000f / 20; // default speed - 50 ms

  @Override
  public void onEnabled() {
    updateTimer();
  }

  @Override
  public void onDisabled() {
    setSpeed(DEFAULT_SPEED);
  }

  private void updateTimer() {
    setSpeed(DEFAULT_SPEED / factor.getAsFloat());
  }

  private void setSpeed(float value) {
    Timer timer = FastReflection.Fields.Minecraft_timer.get(MC);
    FastReflection.Fields.Timer_tickLength.set(timer, value);
  }

  @Override
  public String getDebugDisplayText() {
    return String.format("%s[%.2f]", super.getDebugDisplayText(), factor.get());
  }
}
