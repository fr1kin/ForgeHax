package com.matt.forgehax.util.key;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Created on 6/10/2017 by fr1kin
 */
public interface IKeyBind {
  
  void bind(int keyCode);
  
  KeyBinding getBind();
  
  void onKeyPressed();
  
  void onKeyDown();
  
  default void unbind() {
    bind(Keyboard.KEY_NONE);
  }
}
