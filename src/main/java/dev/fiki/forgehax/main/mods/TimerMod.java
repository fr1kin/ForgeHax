package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.mods.services.TickRateService;
import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.cmd.settings.FloatSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.Timer;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created by Babbaj on 1/24/2018.
 */
@RegisterMod
public class TimerMod extends ToggleMod {

  public TimerMod() {
    super(Category.MISC, "Timer", false, "Speed up game time");
  }

  public final FloatSetting factor = newFloatSetting()
      .name("speed")
      .description("how fast to make the game run")
      .defaultTo(1f)
      .min(0f)
      .changedListener((from, to) -> {
        if (this.isEnabled()) {
          updateTimer();
        }
      })
      .build();

  public final BooleanSetting tpsSync = newBooleanSetting()
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
    if (!tpsSync.getValue()) {
      setSpeed(DEFAULT_SPEED / factor.getValue());
    }
  }

  @SubscribeEvent
  public void onPacketPreceived(PacketInboundEvent event) {
    if (event.getPacket() instanceof SUpdateTimePacket && tpsSync.getValue()) {
      TickRateService monitor = TickRateService.getInstance();
      if (!monitor.isEmpty()) {
        setSpeed((float) (DEFAULT_SPEED / (monitor.getTickrate() / 20.f)));
      }
    } else {
      updateTimer();
    }
  }

  private void setSpeed(float value) {
    Timer timer = FastReflection.Fields.Minecraft_timer.get(Common.MC);
    FastReflection.Fields.Timer_tickLength.set(timer, value);
  }

  @Override
  public String getDisplayText() {
    if (tpsSync.getValue()) {
      TickRateService monitor = TickRateService.getInstance();
      if (!monitor.isEmpty()) {
        return String.format("%s[%.2f]", super.getDisplayText(), monitor.getTickrate() / 20);
      }
    } else {
      return String.format("%s[%.2f]", super.getDisplayText(), factor.getValue());
    }
    return super.getDisplayText();
  }
}
