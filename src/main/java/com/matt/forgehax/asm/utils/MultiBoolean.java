package com.matt.forgehax.asm.utils;

import com.google.common.collect.Sets;
import java.util.Set;

/**
 * Created on 5/12/2017 by fr1kin
 */
public class MultiBoolean {
  
  /**
   * A list of unique string ids so that one mod cannot increment the level more than once.
   */
  private final Set<String> ids = Sets.newCopyOnWriteArraySet();

  private int level = 0;

  private void clampLevel() {
    level = Math.max(0, Math.min(ids.size(), level));
  }

  /**
   * Effectively enables this object
   *
   * @param uniqueId id used to identify this increment
   */
  public void enable(String uniqueId) {
    if (ids.add(uniqueId)) {
      ++level;
      clampLevel();
    }
  }

  /**
   * Disables this object for the specific id
   *
   * @param uniqueId unique id
   */
  public void disable(String uniqueId) {
    if (ids.remove(uniqueId)) {
      --level;
      clampLevel();
    }
  }

  /**
   * Will clear the id list and set the level to zero, disabling this object Do not use this unless
   * it's absolutely necessary
   */
  public void forceDisable() {
    level = 0;
    ids.clear();
  }

  /**
   * Check if the object is enabled
   *
   * @return true if the level is above zero
   */
  public boolean isEnabled() {
    return level > 0;
  }
}
