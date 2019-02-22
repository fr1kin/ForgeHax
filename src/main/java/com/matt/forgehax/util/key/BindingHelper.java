package com.matt.forgehax.util.key;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import org.apache.commons.lang3.NotImplementedException;

public class BindingHelper {
  private static final Map<Integer, String> MOUSE_CODES = Maps.newHashMap();
  private static final IKeyConflictContext EMPTY =
      new IKeyConflictContext() {
        @Override
        public boolean isActive() {
          return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
          return false;
        }
      };

  static {
    MOUSE_CODES.put(-100, "MOUSE_LEFT");
    MOUSE_CODES.put(-99, "MOUSE_RIGHT");
    MOUSE_CODES.put(-98, "MOUSE_MIDDLE");
  }


  public static String getIndexName(int code) {
    throw new NotImplementedException("getIndexName");
    /*if (MOUSE_CODES.get(code) != null) return MOUSE_CODES.get(code);
    else if (code < 0) return Mouse.getButtonName(100 + code);
    else return Keyboard.getKeyName(code);*/
  }

  public static String getIndexName(KeyBinding binding) {
    return getIndexName(binding.getKey().getKeyCode());
  }

  public static IKeyConflictContext getEmptyKeyConflictContext() {
    return EMPTY;
  }
}
