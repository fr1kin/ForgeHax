package com.matt.forgehax.util.filter;

import com.google.common.collect.Maps;
import java.util.Map;

/** Created on 8/23/2017 by fr1kin */
public class FilterFactory {
  private static final Map<String, Class<? extends FilterElement>> FILTERS =
      Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

  public static void register(String name, Class<? extends FilterElement> clazz) {
    FILTERS.put(name, clazz);
  }

  public static void unregister(String name) {
    FILTERS.remove(name);
  }

  public static Class<? extends FilterElement> get(String name) {
    return FILTERS.get(name);
  }

  @SuppressWarnings("unchecked")
  public static <T extends FilterElement> T newInstanceByName(String name) throws Throwable {
    Class<? extends FilterElement> clazz = get(name);
    return clazz != null ? (T) clazz.newInstance() : null;
  }
}
