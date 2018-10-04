package com.matt.forgehax.mods.services;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 11/14/2016 by fr1kin */
@RegisterMod
public class TickRateService extends ServiceMod {
  /** Ticks per second maximum and minimum */
  public static final double MAX_TICKRATE = 20.D;

  public static final double MIN_TICKRATE = 0.D;

  /** Tick rate sample size */
  public static final int MAXIMUM_SAMPLE_SIZE = 100;

  private static final TickRateService INSTANCE = new TickRateService();
  private static final TickRateData TICK_DATA = new TickRateData(MAXIMUM_SAMPLE_SIZE);

  public static TickRateService getInstance() {
    return INSTANCE;
  }

  public static TickRateData getTickData() {
    return TICK_DATA;
  }

  private long timeLastTimeUpdate = -1;

  public TickRateService() {
    super("TickManager", "Records the average tick rate");
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    timeLastTimeUpdate = -1;
    TICK_DATA.onWorldLoaded();
  }

  @SubscribeEvent
  public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketTimeUpdate) {
      if (timeLastTimeUpdate != -1) {
        TICK_DATA.onTimePacketIncoming(System.currentTimeMillis() - timeLastTimeUpdate);
      }
      timeLastTimeUpdate = System.currentTimeMillis();
    }
  }

  public static class TickRateData {
    private final CalculationData EMPTY_DATA = new CalculationData();

    private final Queue<Double> rates;
    private final List<CalculationData> data = Lists.newArrayList();

    private TickRateData(int maxSampleSize) {
      this.rates = EvictingQueue.create(maxSampleSize);
      for (int i = 0; i < maxSampleSize; i++) data.add(new CalculationData());
    }

    private void resetData() {
      for (CalculationData d : data) d.reset();
    }

    private void recalculate() {
      resetData();
      int size = 0;
      double total = 0.D;
      List<Double> in = Lists.newArrayList(rates);
      Collections.reverse(in);
      for (Double rate : in) {
        size++;
        total += rate;
        CalculationData d = data.get(size - 1);
        if (d != null) {
          d.average = MathHelper.clamp(total / (double) (size), MIN_TICKRATE, MAX_TICKRATE);
        }
      }
    }

    public CalculationData getPoint(int point) {
      // clamp between existing data points and 0
      CalculationData d = data.get(Math.max(Math.min(getSampleSize() - 1, point - 1), 0));
      return d != null ? d : EMPTY_DATA;
    }

    public CalculationData getPoint() {
      return getPoint(getSampleSize() - 1);
    }

    public int getSampleSize() {
      return rates.size();
    }

    private void onTimePacketIncoming(long difference) {
      double timeElapsed = ((double) (difference) / 1000.D);
      rates.offer(MathHelper.clamp(MAX_TICKRATE / timeElapsed, MIN_TICKRATE, MAX_TICKRATE));
      // recalculate tick rate data
      recalculate();
    }

    private void onWorldLoaded() {
      rates.clear();
      resetData();
    }

    public class CalculationData {
      private double average = 0.D;

      public double getAverage() {
        return average;
      }

      public void reset() {
        average = 0.D;
      }
    }
  }
}
