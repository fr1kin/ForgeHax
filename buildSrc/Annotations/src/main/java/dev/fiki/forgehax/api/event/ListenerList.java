package dev.fiki.forgehax.api.event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public final class ListenerList implements Iterable<EventListener> {
  private static final Comparator<EventListener> EVENT_LISTENER_COMPARATOR =
      Comparator.comparing(EventListener::getPriority);

  // the event this listener list represents
  private final Class<?> eventType;
  // the super class(es) of the event that will be updated in unison
  private final List<ListenerList> superListeners = new ArrayList<>();
  // a mutex to synchronize updating the listeners field
  private final Lock mutex = new ReentrantLock();
  // a reference to an immutable list that can be atomically updated
  private volatile List<EventListener> listeners = Collections.emptyList();

  public ListenerList(Class<?> eventType) {
    this.eventType = Objects.requireNonNull(eventType, "event type cannot be null");

    try {
      // add this listener to another listeners list of super listeners
      for (Class<?> parent = eventType.getSuperclass();
           parent != null
               && parent != Event.class
               && Event.class.isAssignableFrom(parent)
               && !Modifier.isAbstract(parent.getModifiers())
               && Modifier.isPublic(parent.getModifiers());
           parent = parent.getSuperclass()) {
        // get the default constructor
        // this should 100% exist. if it doesn't, the javac plugin
        // did not execute
        Constructor<?> con = parent.getConstructor();
        con.setAccessible(true);
        Event inst = (Event) con.newInstance();
        ListenerList listeners = inst.getListenerList();

        // use the others mutex to lock and safely add this class to its super list
        listeners.mutex.lock();
        try {
          listeners.superListeners.add(this);
        } finally {
          listeners.mutex.unlock();
        }
      }
    } catch (Throwable t) {
      throw new Error("Error discovering events parent listeners", t);
    }
  }

  /**
   * The Event this ListenerList represents
   *
   * @return class of event
   */
  public Class<?> getEventType() {
    return eventType;
  }

  /**
   * Registers a list of listeners. Updates them atomically
   *
   * @param listeners new listeners to add
   */
  public void registerAll(Collection<EventListener> listeners) {
    if (!listeners.isEmpty()) {

      // we don't want to be modifying the listeners at the same time
      // so we force them to be added in sync
      mutex.lock();
      try {
        List<EventListener> mutable = new ArrayList<>(this.listeners);
        mutable.addAll(listeners);
        mutable.sort(EVENT_LISTENER_COMPARATOR);

        this.listeners = Collections.unmodifiableList(mutable);

        // update all sub listeners
        for (ListenerList subList : superListeners) {
          subList.registerAll(listeners);
        }
      } finally {
        mutex.unlock();
      }
    }
  }

  /**
   * Register a single listener.
   *
   * @param listener listener to add
   */
  public void register(EventListener listener) {
    registerAll(Collections.singleton(listener));
  }

  /**
   * Unregisters a list of listeners.
   *
   * @param listeners listeners to remove
   */
  public void unregisterAll(Collection<EventListener> listeners) {
    if (!listeners.isEmpty()) {

      mutex.lock();
      try {
        List<EventListener> mutable = new ArrayList<>(this.listeners);
        mutable.removeAll(listeners);
        // list should still be in order, no need to resort

        this.listeners = Collections.unmodifiableList(mutable);

        // update all sub listeners
        for (ListenerList subList : superListeners) {
          subList.unregisterAll(listeners);
        }
      } finally {
        mutex.unlock();
      }
    }
  }

  /**
   * Unregister a listener
   *
   * @param listener Listener to remove
   */
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
    return listeners.stream();
  }
}
