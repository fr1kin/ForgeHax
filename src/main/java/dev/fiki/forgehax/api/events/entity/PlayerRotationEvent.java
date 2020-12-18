package dev.fiki.forgehax.api.events.entity;

import dev.fiki.forgehax.api.event.Event;
import dev.fiki.forgehax.api.math.Angle;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerRotationEvent extends Event {
  private final Angle clientViewAngles;
  private Angle viewAngles = null;

  private boolean updated = false;

  @Setter
  private boolean silent = true;

  private Runnable focusTask = null;

  public PlayerRotationEvent(Angle clientViewAngles, Angle serverViewAngles) {
    this.clientViewAngles = clientViewAngles;
    this.viewAngles = serverViewAngles;
  }

  public void setViewAngles(Angle viewAngles) {
    this.viewAngles = viewAngles;
    this.updated = true;
  }

  public void onFocusGained(Runnable inFocusTask) {
    this.focusTask = inFocusTask;
  }
}
