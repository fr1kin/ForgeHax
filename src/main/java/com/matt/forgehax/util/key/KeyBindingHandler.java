package com.matt.forgehax.util.key;

import com.matt.forgehax.ForgeHax;
import net.minecraft.client.settings.KeyBinding;

public abstract class KeyBindingHandler {

  public static KeyBindingHandler newKeyBindingHandler(KeyBinding keyBind) {
    if (ForgeHax.isForge())
      return new ForgeKeyBindingHandler(keyBind);
    else
      return new VanillaKeyBindingHandler(keyBind);
  }

  protected final KeyBinding binding;

  public KeyBindingHandler(KeyBinding keyBind) {
    this.binding = keyBind;
  }

  public final KeyBinding getBinding() {
    return binding;
  }

  public final void setPressed(boolean b) {
    // press key
    KeyBinding.setKeyBindState(binding.getKeyCode(), b);
  }

  public boolean isBound() {
    return true;
  }

  public void bind() { }

  public void attemptBind() { }

  public void unbind() { }

  public void attemptUnbind() { }

}
