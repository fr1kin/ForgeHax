package dev.fiki.forgehax.main.util.math;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ScreenPos {
  private final double x;
  private final double y;
  private final boolean visible;

  public int getXAsInteger() {
    return (int) getX();
  }

  public int getYAsInteger() {
    return (int) getY();
  }
}
