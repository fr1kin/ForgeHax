package dev.fiki.forgehax.api.event;

public class Event {
  public Event() {}

  public ListenerList getListenerList() {
    throw new UnsupportedOperationException();
  }

  public static ListenerList listenerList() {
    throw new UnsupportedOperationException();
  }

  public boolean isCanceled() {
    return false;
  }

  public void setCanceled(boolean canceled) {
    throw new UnsupportedOperationException("Cannot cancel this event");
  }
}
