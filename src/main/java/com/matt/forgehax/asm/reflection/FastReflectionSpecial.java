package com.matt.forgehax.asm.reflection;

import com.matt.forgehax.asm.utils.fasttype.FastField;
import com.matt.forgehax.asm.utils.fasttype.FastTypeBuilder;
import java.util.Map;
import org.lwjgl.input.Keyboard;

/**
 * Created on 5/8/2017 by fr1kin
 */
public interface FastReflectionSpecial {
  
  interface Classes {
  
  }
  
  interface Methods {
  
  }
  
  interface Fields {
    
    /**
     * LJGLW Keyboard
     */
    FastField<Map<String, Integer>> Keyboard_keyMap =
      FastTypeBuilder.create().setInsideClass(Keyboard.class).setName("keyMap").asField();
  }
}
