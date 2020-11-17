package dev.fiki.forgehax.api;

import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;

public abstract class Switch {
  
  private final Set<Handle> handles = Sets.newHashSet();
  private final String name;
  
  private int level = 0;
  
  public Switch(String name) {
    this.name = name;
  }
  
  public Handle createHandle(String id) {
    Handle handle = new Handle(this, id);
    synchronized (handles) {
      if (handles.add(handle)) {
        return handle;
      } else {
        throw new Error("failed to add handle with id '" + id + "'");
      }
    }
  }
  
  public boolean removeHandle(Handle handle) {
    synchronized (handles) {
      return handles.remove(handle);
    }
  }
  
  private void enable() {
    level = Math.min(handles.size(), level + 1);
  }
  
  private void disable() {
    level = Math.max(0, level - 1);
  }
  
  public boolean isEnabled() {
    return level > 0;
  }
  
  public boolean isDisabled() {
    return !isEnabled();
  }
  
  protected abstract void onEnabled();
  
  protected abstract void onDisabled();
  
  @Override
  public String toString() {
    return name + "@" + handles.size();
  }
  
  // a more efficient way to toggle the state without having the iterate the array list everytime
  public static class Handle {
    
    private final Switch parent;
    private final String id;
    
    private boolean enabled = false;
    
    private Handle(Switch parent, String id) {
      Objects.requireNonNull(parent, "null parent");
      Objects.requireNonNull(id, "null id");
      this.parent = parent;
      this.id = id;
    }
    
    public void enable() {
      if (!enabled) {
        enabled = true;
        parent.enable();
      }
      
      if (parent.isEnabled()) {
        parent.onEnabled();
      }
    }
    
    public void disable() {
      if (enabled) {
        enabled = false;
        parent.disable();
      }
      
      if (parent.isDisabled()) {
        parent.onDisabled();
      }
    }
    
    @Override
    public int hashCode() {
      return id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof Handle && parent.equals(((Handle) obj).parent)
          && id == ((Handle) obj).id);
    }
    
    @Override
    public String toString() {
      return id;
    }
  }
}
