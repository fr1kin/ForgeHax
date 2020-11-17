package dev.fiki.forgehax.api.cmd.listener;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface IListenable {
  default <T extends ICommandListener> boolean addListener(Class<T> type, T listener) {
    return addListeners(type, Collections.singleton(listener));
  }

  default <T extends ICommandListener> boolean addListener(T listener) {
    return addListeners(listener.getListenerClassType(), Collections.singleton(listener));
  }

  boolean addListeners(Class<? extends ICommandListener> type, Collection<? extends ICommandListener> listener);

  <T extends ICommandListener> List<T> getListeners(Class<T> type);
}
