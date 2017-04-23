package com.matt.forgehax.util;

import com.google.common.collect.EvictingQueue;
import com.matt.forgehax.Globals;
import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 11/14/2016 by fr1kin
 */
public class TickManager implements Globals {
    private static final TickManager INSTANCE = new TickManager();

    public static TickManager getInstance() {
        return INSTANCE;
    }

    /**
     * Ticks per second maximum and minimum
     */
    public static final double MAX_TICKRATE = 20.D;
    public static final double MIN_TICKRATE = 0.D;

    /**
     * Tick rate sample size
     */
    public static final int MAXIMUM_SAMPLE_SIZE = 100;

    private LagCompensatorEventHandler events;

    private final Queue<Double> rates = EvictingQueue.create(MAXIMUM_SAMPLE_SIZE);
    private final TickRateData data = new TickRateData();

    public TickManager() {}

    public void registerEventHandler() {
        if(events == null) MinecraftForge.EVENT_BUS.register(events = new LagCompensatorEventHandler());
    }

    public void unregisterEventHandler() {
        if(events != null) {
            MinecraftForge.EVENT_BUS.unregister(events);
            events = null;
        }
    }

    public TickRateData getData() {
        return data;
    }

    public class TickRateData {
        private int sampleSize = 0;

        private double average = 0.D;

        private void update() {
            this.sampleSize = rates.size();

            double total = 0.D;
            for(Double rate : rates) total += rate;

            this.average = MathHelper.clamp(total / (double)(this.sampleSize), MIN_TICKRATE, MAX_TICKRATE);
        }

        public int getSampleSize() {
            return sampleSize;
        }

        public double getAverage() {
            return average;
        }
    }

    private class LagCompensatorEventHandler {
        private long timeLastTimeUpdate = -1;

        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) {
            timeLastTimeUpdate = -1;
            rates.clear();
            data.update();
        }

        @SubscribeEvent
        public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
            if(event.getPacket() instanceof SPacketTimeUpdate) {
                if(timeLastTimeUpdate != -1) {
                    SPacketTimeUpdate packet = (SPacketTimeUpdate)event.getPacket();
                    // how long (in seconds) it took the server to complete 20 ticks
                    double timeElapsed = ((double)(System.currentTimeMillis() - timeLastTimeUpdate) / 1000.D);
                    rates.add(MathHelper.clamp(MAX_TICKRATE / timeElapsed, MIN_TICKRATE, MAX_TICKRATE));
                    // update tick rate data
                    data.update();
                }
                timeLastTimeUpdate = System.currentTimeMillis();
            }
        }
    }
}
