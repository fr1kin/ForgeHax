package com.matt.forgehax.util.gui.callbacks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.List;
import java.util.Objects;

/** Created on 9/16/2017 by fr1kin */
public class CallbackList {
  private final Multimap<Class<? extends IGuiCallbackBase>, IGuiCallbackBase> callbacks =
      Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);

  public <T extends IGuiCallbackBase> boolean add(Class<T> clazz, T callback) {
    Objects.requireNonNull(callback);
    return callbacks.put(clazz, callback);
  }

  public <T extends IGuiCallbackBase> boolean remove(Class<T> clazz, T callback) {
    Objects.requireNonNull(callback);
    return callbacks.remove(clazz, callback);
  }

  public <T extends IGuiCallbackBase> List<T> get(Class<T> clazz) {
    return (List<T>) callbacks.get(clazz);
  }
}
