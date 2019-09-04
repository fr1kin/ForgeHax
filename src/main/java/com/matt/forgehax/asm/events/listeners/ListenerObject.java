package com.matt.forgehax.asm.events.listeners;

import com.google.common.collect.Sets;
import java.util.Collection;

/**
 * Created on 5/12/2017 by fr1kin
 */
public class ListenerObject<E> {
  
  private Collection<E> listeners = Sets.newConcurrentHashSet();

  public void register(E listener) {
    listeners.add(listener);
  }

  public void unregister(E listener) {
    listeners.remove(listener);
  }

  public Collection<E> getAll() {
    return listeners;
  }
}
