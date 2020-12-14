package dev.fiki.forgehax.api.event;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ListenerList implements Iterable<EventListener> {
  private static final Comparator<EventListener> EVENT_LISTENER_COMPARATOR =
      Comparator.comparing(EventListener::getPriority);

  private final Class<?> eventType;
  private volatile List<EventListener> listeners = Collections.emptyList();

  public ListenerList(Class<?> eventType) {
    this.eventType = Objects.requireNonNull(eventType, "event type cannot be null");
  }

  public Class<?> getEventType() {
    return eventType;
  }

  public synchronized void registerAll(Collection<EventListener> listeners) {
    List<EventListener> mutable = new ArrayList<>(this.listeners);
    mutable.addAll(listeners);
    mutable.sort(EVENT_LISTENER_COMPARATOR);

    this.listeners = Collections.unmodifiableList(mutable);
  }

  public void register(EventListener listener) {
    registerAll(Collections.singleton(listener));
  }

  public synchronized void unregisterAll(Collection<EventListener> listeners) {
    List<EventListener> mutable = new ArrayList<>(this.listeners);
    mutable.removeAll(listeners);

    this.listeners = Collections.unmodifiableList(mutable);
  }

  public void unregister(EventListener listener) {
    unregisterAll(Collections.singleton(listener));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ListenerList that = (ListenerList) o;

    return eventType.equals(that.eventType);
  }

  @Override
  public int hashCode() {
    return eventType.hashCode();
  }

  @Override
  public Iterator<EventListener> iterator() {
    return listeners.listIterator();
  }

  public Stream<EventListener> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
