package dev.fiki.forgehax.api.event;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

public class EventBus {
  private static final Map<Class<?>, Event> EVENT_FACTORY_CACHE = Maps.newConcurrentMap();

  public void register(Object obj) {
    // only get visible methods
    for (Method method : obj.getClass().getMethods()) {
      if (method.isAnnotationPresent(SubscribeListener.class)) {
        Event event = getEventFactory(getMethodEventType(method));
        EventListenerWrapper listener = new EventListenerWrapper(obj, method);
        event.getListenerList().register(listener);
      }
    }
  }

  public void unregister(Object obj) {
    // remove all listeners associated with this object
    for (Event event : EVENT_FACTORY_CACHE.values()) {
      ListenerList listeners = event.getListenerList();
      listeners.unregisterAll(listeners.stream()
          .filter(EventListenerWrapper.class::isInstance)
          .map(EventListenerWrapper.class::cast)
          .filter(e -> e.getDeclaringInstance() == obj)
          .collect(Collectors.toList()));
    }
  }

  public <T extends Event> boolean post(T event) {
    for (EventListener listener : event.getListenerList()) {
      if (ListenerFlags.present(listener, ListenerFlags.ALLOW_CANCELED) || !event.isCanceled()) {
        listener.run(event);
      }
    }
    return event.isCanceled();
  }

  private static Event getEventFactory(Class<?> eventClass) {
    return EVENT_FACTORY_CACHE.computeIfAbsent(eventClass, EventBus::createNewEventFactory);
  }

  @SneakyThrows
  private static Event createNewEventFactory(Class<?> clazz) {
    // try and get default constructor
    Constructor<?> constructor = clazz.getConstructor();
    constructor.setAccessible(true);
    // create new object
    return (Event) constructor.newInstance();
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
