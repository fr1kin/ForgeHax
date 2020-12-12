package dev.fiki.forgehax.api.event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListenerList {
  private volatile List<EventListener> listeners = Collections.emptyList();

  public ListenerList(Class<?> classType) {

  }

  public void registerAll(Collection<EventListener> listeners) {

  }
}
