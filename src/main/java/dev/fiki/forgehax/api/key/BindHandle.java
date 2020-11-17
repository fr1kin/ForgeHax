package dev.fiki.forgehax.api.key;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.Objects;

class BindHandle {
  @Getter
  private final KeyBinding key;
  private int level;

  @Getter
  private boolean disabled = false;

  private IKeyConflictContext oldContext = null;
  @Setter
  private IKeyConflictContext overrideContext;

  public BindHandle(KeyBinding key) {
    this.key = key;
    this.level = 0;
    this.overrideContext = BindingHelper.getEmptyKeyConflictContext();
  }

  boolean isRestored() {
    return level <= 0;
  }

  public void restoreContext() {
    if(--level <= 0 && disabled) {
      disabled = false;
      key.setKeyConflictContext(oldContext);
      oldContext = null;
    }

    // make sure this doesn't go negative
    level = Math.max(0, level);
  }

  public void disableContext() {
    if(!disabled) {
      disabled = true;
      oldContext = key.getKeyConflictContext();
      key.setKeyConflictContext(overrideContext);
    }
    level++;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BindHandle that = (BindHandle) o;
    return Objects.equals(key, that.key);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key);
  }
}
