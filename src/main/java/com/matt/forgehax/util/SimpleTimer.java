package com.matt.forgehax.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/** Created on 8/22/2017 by fr1kin */
public class SimpleTimer {
  private static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  static {
    TIME_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public static String toFormattedTime(long time) {
    return TIME_FORMATTER.format(new Date(time));
  }

  public static String toFormattedDate(long time) {
    return DATE_FORMATTER.format(new Date(time));
  }

  private long timeStarted = -1;
  private long timeStopped = -1;

  public SimpleTimer(boolean startOnInit) {
    if (startOnInit) start();
  }

  public SimpleTimer() {
    this(false);
  }

  public void start() {
    timeStarted = System.currentTimeMillis();
  }

  public void stop() {
    timeStopped = System.currentTimeMillis();
  }

  public void reset() {
    timeStarted = timeStopped = -1;
  }

  public boolean isStarted() {
    return timeStarted > -1;
  }

  public boolean isStopped() {
    return timeStopped > -1;
  }

  public long getTimeStarted() {
    return Math.max(timeStarted, 0L);
  }

  public long getTimeStopped() {
    return Math.max(timeStopped, 0L);
  }

  private long _time() {
    return isStopped() ? getTimeStopped() : System.currentTimeMillis();
  }

  public long getTimeElapsed() {
    return _time() - getTimeStarted();
  }

  public boolean hasTimeElapsed(long time) {
    return time <= getTimeElapsed();
  }

  public String getFormattedTimeStarted() {
    return DATE_FORMATTER.format(new Date(getTimeStarted()));
  }

  public String getFormattedTimeStopped() {
    return DATE_FORMATTER.format(new Date(getTimeStopped()));
  }

  public String getFormattedTimeElapsed() {
    return TIME_FORMATTER.format(new Date(getTimeElapsed()));
  }
}
