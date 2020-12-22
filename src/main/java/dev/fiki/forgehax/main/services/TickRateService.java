package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.SimpleTimer;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.ConnectToServerEvent;
import dev.fiki.forgehax.api.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import net.minecraft.network.play.server.SUpdateTimePacket;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Created on 11/14/2016 by fr1kin
 */
@RegisterMod
public class TickRateService extends ServiceMod {

  /**
   * Ticks per second maximum and minimum
   */
  public static final double MAX_TICKRATE = 20.D;
  public static final double MIN_TICKRATE = 0.D;

  public static double clampTickrate(double rate) {
    return Math.min(MAX_TICKRATE, Math.max(MIN_TICKRATE, rate));
  }

  private final IntegerSetting sampleSize = newIntegerSetting()
      .name("sample-size")
      .description("Number of ticks to record")
      .defaultTo(60)
      .min(1)
      .changedListener(((from, to) -> updateTickDelayArray(to)))
      .build();

  private TickrateTimer[] data = new TickrateTimer[sampleSize.getDefaultValue()];
  private int currentIndex;

  private int currentCount = 0;
  private long currentTotal = 0;
  private double currentTickrate;

  private void resetData() {
    Arrays.fill(data, null);
    currentIndex = 0;
    currentCount = 0;
    currentTotal = 0;
    currentTickrate = 0;
  }

  private void updateTickDelayArray(int newSize) {
    // its possible (but extremely unlikely) this could cause problems
    data = new TickrateTimer[newSize];
    resetData();
  }

  private double calculateAverage(int count, long total) {
    if (count != 0) {
      // prevent dividing by 0
      return clampTickrate((double) total / (double) count);
    }
    return MAX_TICKRATE;
  }

  private void calculateCurrentTickrate() {
    int count = 0;
    long total = 0;
    for (TickrateTimer timer : data) {
      if (timer != null && timer.isStopped()) {
        count++;
        total += timer.getTickrate();
      }
    }
    currentCount = count;
    currentTotal = total;
    currentTickrate = calculateAverage(currentCount, currentTotal);
  }

  public boolean isEmpty() {
    return currentCount == 0;
  }

  public double getTickrate() {
    return currentTickrate;
  }

  public double getRealtimeTickrate() {
    int count = currentCount;
    long total = currentTotal;

    // add the still running tick monitor to the
    TickrateTimer timer = data[currentIndex];
    if (timer != null && timer.isRunning()) {
      count++;
      total += timer.getTickrate();
    }
    return calculateAverage(count, total);
  }

  @Nullable
  public TickrateTimer getCurrentTimer() {
    return data[currentIndex];
  }

  @Override
  protected void onLoad() {
    // update data array if the size if different
    if (data.length != sampleSize.getValue()) {
      updateTickDelayArray(sampleSize.getValue());
    }
  }

  @SubscribeListener
  public void onConnect(ConnectToServerEvent event) {
    // reset all tick data
    resetData();
  }

  @SubscribeListener
  public void onDisconnect(DisconnectFromServerEvent event) {
    // reset all tick data
    resetData();
  }

  @SubscribeListener
  public void onPacketInbound(PacketInboundEvent event) {
    if (event.getPacket() instanceof SUpdateTimePacket) {
      SimpleTimer timer = data[currentIndex];
      if (timer == null) {
        // this is the first time packet the player will receive
        data[currentIndex] = new TickrateTimer(true);
        // the new time should be started
      } else {
        if (!timer.isStarted()) {
          // :thinking:
          log.warn("TickMonitor timer not started, this should not happen!");
          timer.start();
        } else {
          // stop current timer and start the next timer
          timer.stop();
          // calculate the current tickrate
          calculateCurrentTickrate();
          // move to the next index
          currentIndex = ++currentIndex % data.length;
          // create and start the next timer
          SimpleTimer next = data[currentIndex];
          if (next == null) {
            // if no timer object exists currently, create a new one
            data[currentIndex] = new TickrateTimer(true);
          } else {
            // otherwise reset the current only and start it
            next.reset();
            next.start();
          }
        }
      }
    }
  }

  public static class TickrateTimer extends SimpleTimer {
    public TickrateTimer(boolean startOnInit) {
      super(startOnInit);
    }

    public double getTickrate() {
      double timeElapsed = getTimeElapsed() / 1000.D;
      return clampTickrate(MAX_TICKRATE / timeElapsed);
    }
  }
}
