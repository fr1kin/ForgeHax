package com.matt.forgehax.util.task;

import com.google.common.collect.Maps;
import java.util.Map;

/** Created on 8/5/2017 by fr1kin */
public interface IProcess {
  void process(DataEntry data);

  class DataEntry {
    private final Map<String, Object> data = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    public <T> T getOrDefault(String o, T defaultValue) {
      try {
        return (T) data.get(o);
      } catch (Throwable t) {
        return defaultValue;
      }
    }

    public <T> T get(String o) {
      return getOrDefault(o, null);
    }

    public void set(String name, Object o) {
      data.put(name, o);
    }
  }
}
