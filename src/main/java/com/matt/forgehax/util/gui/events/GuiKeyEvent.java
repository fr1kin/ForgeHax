package com.matt.forgehax.util.gui.events;

/** Created on 9/10/2017 by fr1kin */
public class GuiKeyEvent {
  public enum Type {
    PRESSED,
    DOWN,
    RELEASED,
    ;
  }

  private final Type type;

  private final int keyCode;
  private final int ticks;

  private final long timePreviouslyPressed;
  private final long timePressed;

  public GuiKeyEvent(
      Type type, int keyCode, int ticks, long timePreviouslyPressed, long timePressed) {
    this.type = type;
    this.keyCode = keyCode;
    this.ticks = ticks;
    this.timePreviouslyPressed = timePreviouslyPressed;
    this.timePressed = timePressed;
  }

  /**
   * Key event type
   *
   * @return type of key event
   */
  public Type getType() {
    return type;
  }

  /**
   * Key code for event
   *
   * @return key code
   */
  public int getKeyCode() {
    return keyCode;
  }

  /**
   * Number of ticks that the key has been held down for
   *
   * @return ticks key down
   */
  public int getTicksDown() {
    return ticks;
  }

  /**
   * Time of the previous key press
   *
   * @return
   */
  public long getTimePreviouslyPressed() {
    return timePreviouslyPressed;
  }

  public long getTimePressed() {
    return timePressed;
  }
}
