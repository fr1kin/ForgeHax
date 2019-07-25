package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.mods.services.TickRateService;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

  public final Setting<Boolean> tpsSync =
          getCommandStub()
                  .builders()
                  .<Boolean>newSettingBuilder()
                  .name("tps-sync")
                  .description("sync timer to tps")
                  .defaultTo(false)
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
      if (!tpsSync.getAsBoolean()) {
          setSpeed(DEFAULT_SPEED / factor.getAsFloat());
      }
  }

    @SubscribeEvent
    public void onPacketPreceived (PacketEvent.Incoming.Pre event){
        if (event.getPacket() instanceof SPacketTimeUpdate && tpsSync.getAsBoolean()) {
            TickRateService.TickRateData data = TickRateService.getTickData();
            if (data.getSampleSize() > 0) {
                TickRateService.TickRateData.CalculationData point = data.getPoint();
                setSpeed((float) (DEFAULT_SPEED / (point.getAverage() / 20)));
            }
        }else {
            updateTimer();
        } 
    }

  private void setSpeed(float value) {
    Timer timer = FastReflection.Fields.Minecraft_timer.get(MC);
    FastReflection.Fields.Timer_tickLength.set(timer, value);
  }

  @Override
  public String getDisplayText() {
      if (tpsSync.getAsBoolean()) {
          TickRateService.TickRateData data = TickRateService.getTickData();
          if (data.getSampleSize() > 0) {
              TickRateService.TickRateData.CalculationData point = data.getPoint();
              return String.format("%s[%.2f]", super.getDisplayText(), point.getAverage() / 20);
          }
      }else {
          return String.format("%s[%.2f]", super.getDisplayText(), factor.get());
      } return super.getDisplayText();
  }
}
