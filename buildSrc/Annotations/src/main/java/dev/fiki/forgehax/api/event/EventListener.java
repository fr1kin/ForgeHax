package dev.fiki.forgehax.api.event;

public interface EventListener extends Comparable<EventListener> {
  void run(Event event);
}
