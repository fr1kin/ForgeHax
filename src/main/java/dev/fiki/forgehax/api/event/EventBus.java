package dev.fiki.forgehax.api.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.Tuple;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventBus {
  private static final Map<Class<?>, Event> EVENT_FACTORY_CACHE = Maps.newConcurrentMap();

  private final Map<Integer, List<Tuple<EventListener, Event>>> trackedListeners = Maps.newHashMap();

  public void register(Object obj) {
    final Class<?> objClass = obj.getClass();

    // list of active listeners
    List<Tuple<EventListener, Event>> tracks = Lists.newArrayList();

    // only get visible methods
    for (Method method : objClass.getMethods()) {
      if (method.isAnnotationPresent(SubscribeListener.class)) {
        Event event = getEventFactory(getMethodEventType(method));
        EventListener listener = new EventListenerWrapper(obj, method);
        event.getListenerList().register(listener);
        tracks.add(new Tuple<>(listener, event));
      }
    }

    synchronized (trackedListeners) {
      trackedListeners.put(System.identityHashCode(obj), tracks);
    }
  }

  public void unregister(Object obj) {
    final int id = System.identityHashCode(obj);

    synchronized (trackedListeners) {
      List<Tuple<EventListener, Event>> tracked = trackedListeners.get(id);

      if (tracked != null) {
        for (Tuple<EventListener, Event> tuple : tracked) {
          tuple.getSecond().getListenerList().unregister(tuple.getFirst());
        }

        trackedListeners.remove(id);
      }
    }
  }

  public <T extends Event> void post(T event) {
    for (EventListener listener : event.getListenerList()) {
      listener.run(event);
    }
  }

  List<EventListener> getObjectListeners(Object obj) {
    synchronized (trackedListeners) {
      List<Tuple<EventListener, Event>> list = trackedListeners.get(System.identityHashCode(obj));
      if (list != null) {
        return list.stream()
            .map(Tuple::getFirst)
            .collect(Collectors.toList());
      } else {
        return Collections.emptyList();
      }
    }
  }

  private static Event getEventFactory(Class<?> eventClass) {
    return EVENT_FACTORY_CACHE.computeIfAbsent(eventClass, EventBus::createNewEventFactory);
  }

  @SneakyThrows
  private static Event createNewEventFactory(Class<?> clazz) {
    return (Event) clazz.newInstance();
  }

  private static Class<?> getMethodEventType(Method method) {
    if (method.getParameterCount() != 1) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" must have exactly 1 argument!");
    }

    Class<?> type = method.getParameterTypes()[0];
    if (!Event.class.isAssignableFrom(type)) {
      throw new IllegalArgumentException("Method \"" + method.getName() + "\" argument must be assignable form of Event!");
    }
    return type;
  }
}
