package dev.fiki.forgehax.api.task;

import dev.fiki.forgehax.api.common.PriorityEnum;

/**
 * Created on 8/5/2017 by fr1kin
 */
public interface IListener<T> {

  boolean register(T func, PriorityEnum priority);

  boolean register(T func);

  boolean registerTemporary(T func, PriorityEnum priority);

  boolean registerTemporary(T func);

  void unregister(T func);

  boolean registerListener(SimpleManagerContainer.Listener<T> listener);

  boolean unregisterListener(SimpleManagerContainer.Listener<T> listener);
}
