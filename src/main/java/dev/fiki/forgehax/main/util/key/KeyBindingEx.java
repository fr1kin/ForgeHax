package dev.fiki.forgehax.main.util.key;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
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
    return FastReflection.Fields.KeyBinding_pressTime.get(this);
  }

  public boolean isKeyDownUnchecked() {
    return FastReflection.Fields.KeyBinding_pressed.get(this);
  }

  @Override
  public void bind(InputMappings.Input key) {
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
    super.bind(key);
  }

  public interface IBindChangeCallback {
    void onChange(InputMappings.Input value);
  }
}
