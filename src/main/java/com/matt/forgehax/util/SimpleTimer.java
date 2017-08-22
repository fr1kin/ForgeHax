package com.matt.forgehax.util;

/**
 * Created on 8/22/2017 by fr1kin
 */
public class SimpleTimer {
    private long timeStarted = -1;

    public void start() {
        timeStarted = System.currentTimeMillis();
    }

    public void stop() {
        timeStarted = -1;
    }

    public boolean isStarted() {
        return timeStarted > -1;
    }

    public boolean hasTimeElapsed(long time) {
        return isStarted() && ((System.currentTimeMillis() + time) > timeStarted);
    }
}
