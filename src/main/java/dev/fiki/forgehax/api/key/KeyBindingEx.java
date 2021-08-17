package dev.fiki.forgehax.api.key;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;

import java.util.Objects;

public class KeyBindingEx extends KeyBinding {
  @Getter
  private final IBindChangeCallback changeCallback;

  @Getter @Setter
  private boolean pressedThisTick = false;

  @Builder
  public KeyBindingEx(String description,
      IKeyConflictContext conflictContext,
      InputMappings.Type type,
      int keyCode,
      String category,
      IBindChangeCallback changeCallback) {
    super(description, MoreObjects.firstNonNull(conflictContext, BindingHelper.getEmptyKeyConflictContext()),
        type, keyCode, category);
    this.changeCallback = changeCallback;
  }

  public int getKeyPressedTime() {
    return ReflectionTools.getInstance().KeyBinding_clickCount.get(this);
  }

  public void setKeyPressedTime(int ticks) {
    ReflectionTools.getInstance().KeyBinding_clickCount.set(this, ticks);
  }

  public void incrementPressedTime() {
    setKeyPressedTime(getKeyPressedTime() + 1);
  }

  public boolean isKeyDownUnchecked() {
    return ReflectionTools.getInstance().KeyBinding_isDown.get(this);
  }

  public boolean checkConflicts() {
    return getKeyConflictContext().isActive();
  }

  @Override
  public void setKey(InputMappings.Input key) {
    if(!Objects.equals(key, getKey())) {
      final InputMappings.Input previous = getKey();
      if(changeCallback != null) {
        // use the callback to set the bind
        changeCallback.onChange(key);
      } else {
        // use the default ::bind method
        setBind(key);
      }
    }
  }

  public void setBind(InputMappings.Input key) {
    super.setKey(key);
  }

  public interface IBindChangeCallback {
    void onChange(InputMappings.Input value);
  }
}
