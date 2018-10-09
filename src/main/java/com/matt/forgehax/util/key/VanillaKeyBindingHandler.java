package com.matt.forgehax.util.key;

import net.minecraft.client.settings.KeyBinding;

public class VanillaKeyBindingHandler extends KeyBindingHandler {

  public VanillaKeyBindingHandler(KeyBinding bind) {
    super(bind);
  }

  @Override
  public boolean isBound() {
    return true;
  }
}
