package dev.fiki.forgehax.api.event;

public interface EventListener {
  void run(Event event);

  default int getPriority() {
    return 0;
  }

  default int getFlags() {
    return 0;
  }
}
