package dev.fiki.forgehax.common;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

/**
 * Created on 5/12/2017 by fr1kin
 */
public class StateManager {
  
  /**
   * A list of unique string ids so that one mod cannot increment the level more than once.
   */
  private final Map<Class<?>, StateHandle> handles = Maps.newHashMap();
  
  private int level = 0;

  private void incrementState(StateHandle handle) {
    if(handles.containsValue(handle)) {
      level = Math.max(0, Math.min(handles.size(), level + 1));
    }
    else throw new IllegalStateException("handle for \"" + handle.getHandle() + "\" is not present");
  }

  private void decrementState(StateHandle handle) {
    if(handles.containsValue(handle)) {
      level = Math.max(0, Math.min(handles.size(), level - 1));
    }
    else throw new IllegalStateException("handle for \"" + handle.getHandle() + "\" is not present");
  }

  public StateHandle createHandle(Class<?> clazz) {
    synchronized (handles) {
      if(handles.containsKey(clazz)) {
        throw new IllegalStateException("Cannot add handle to existing class");
      }
      StateHandle handle = new StateHandle(clazz, false);
      handles.put(clazz, handle);
      return handle;
    }
  }

  public void closeHandle(Class<?> clazz) {
    synchronized (handles) {
      if(handles.containsKey(clazz)) {
        StateHandle handle = handles.get(clazz);
        Objects.requireNonNull(handle, "handle does not exist");
        handle.disable();
        handles.remove(clazz);
      }
      else throw new IllegalArgumentException("handle for \"" + clazz + "\" is not present");
    }
  }

  public void enableHandle(Class<?> clazz) {
    synchronized (handles) {
      if(handles.containsKey(clazz)) {
        handles.get(clazz).enable();
      }
      else throw new IllegalArgumentException("handle for \"" + clazz + "\" is not present");
    }
  }

  public void disableHandle(Class<?> clazz) {
    synchronized (handles) {
      if(handles.containsKey(clazz)) {
        handles.get(clazz).disable();
      }
      else throw new IllegalArgumentException("handle for \"" + clazz + "\" is not present");
    }
  }
  
  /**
   * Will clear the handles list and set the level to zero, disabling this object Do not use this unless
   * it's absolutely necessary
   */
  public void closeHandles() {
    synchronized (handles) {
      // set all to disabled (default state)
      level = 0;
      handles.clear();
    }
  }
  
  /**
   * Check if the object is enabled
   *
   * @return true if the level is above zero
   */
  public boolean isEnabled() {
    return level > 0;
  }

  /**
   * There are active handles to this object
   * @return true if there are active handles
   */
  public boolean isActive() {
    return !handles.isEmpty();
  }

  @Getter(AccessLevel.PRIVATE)
  @AllArgsConstructor
  public class StateHandle {
    private final Class<?> handle;
    private boolean enabled;

    public void enable() {
      if(!enabled) {
        enabled = true;
        StateManager.this.incrementState(this);
      }
    }

    public void disable() {
      if(enabled) {
        enabled = false;
        StateManager.this.decrementState(this);
      }
    }

    public void close() {
      StateManager.this.closeHandle(getHandle());
    }

    @Override
    public boolean equals(Object o) {
      return o == this;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this);
    }
  }
}
