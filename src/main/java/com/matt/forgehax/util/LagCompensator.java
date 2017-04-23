package com.matt.forgehax.util;

import com.matt.forgehax.asm.events.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

/**
 * Created on 11/14/2016 by fr1kin
 */
public class LagCompensator {
    private static final LagCompensator INSTANCE = new LagCompensator();

    public static LagCompensator getInstance() {
        return INSTANCE;
    }

    /**
     * Ticks per second maximum and minimum
     */
    private static final int MAX_TICKRATE = 20;
    private static final int MIN_TICKRATE = 0;

    /**
     * Tick rate sample size
     */
    private static final int SAMPLE_SIZE = 100;

    private final float[] tickRates = new float[SAMPLE_SIZE];

    private final LagCompensatorEventHandler eventHandler = new LagCompensatorEventHandler(this);

    private int nextIndex = 0;

    private long timeLastTimeUpdate;

    public LagCompensator() {
        reset();
    }

    public float getTickRate() {
        float numTicks = 0.f;
        float sumTickRates = 0.f;
        for(float tickRate : tickRates) {
            if(tickRate > 0) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }
        return MathHelper.clamp(sumTickRates / numTicks, MIN_TICKRATE, MAX_TICKRATE);
    }

    public int getClampedTickRate() {
        return Math.round(getTickRate());
    }

    public LagCompensatorEventHandler getEventHandler() {
        return eventHandler;
    }

    public void onTimeUpdate() {
        if(timeLastTimeUpdate != -1) {
            // how long (in seconds) it took the server to complete 20 ticks
            float timeElapsed = ((float)(System.currentTimeMillis() - timeLastTimeUpdate) / 1000.f);
            tickRates[nextIndex % tickRates.length] = MathHelper.clamp((float)MAX_TICKRATE / timeElapsed, MIN_TICKRATE, MAX_TICKRATE);
            nextIndex++;
        }
        timeLastTimeUpdate = System.currentTimeMillis();
    }

    public void reset() {
        nextIndex = 0;
        timeLastTimeUpdate = -1;
        Arrays.fill(tickRates, 0.f);
    }

    private class LagCompensatorEventHandler {
        private final LagCompensator lagCompensator;

        public LagCompensatorEventHandler(LagCompensator lagCompensator) {
            this.lagCompensator = lagCompensator;
        }

        @SubscribeEvent
        public void onWorldLoaded(WorldEvent.Load event) {
            lagCompensator.reset();
        }

        @SubscribeEvent
        public void onPacketPreceived(PacketEvent.Incoming.Pre event) {
            if(event.getPacket() instanceof SPacketTimeUpdate) {
                lagCompensator.onTimeUpdate();
            }
        }
    }
}
