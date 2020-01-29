package com.matt.forgehax.util.key;

import com.google.common.collect.Maps;
import java.util.Map;

import com.matt.forgehax.Globals;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;

import static com.matt.forgehax.Globals.getGameSettings;

public class BindingHelper {
  
  private static final Map<Integer, String> MOUSE_CODES = Maps.newHashMap();
  private static final IKeyConflictContext EMPTY = new IKeyConflictContext() {
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
    // TODO: 1.15 using mojangs new InputMappings
    throw new UnsupportedOperationException();
  }
  
  public static String getIndexName(KeyBinding binding) {
    return getIndexName(binding.getKey().getKeyCode());
  }
  
  public static IKeyConflictContext getEmptyKeyConflictContext() {
    return EMPTY;
  }

  public static void addBinding(KeyBinding binding) {
    ClientRegistry.registerKeyBinding(binding);
    KeyBinding.resetKeyBindingArrayAndHash();
  }

  public static boolean removeBinding(KeyBinding binding) {
    int i = ArrayUtils.indexOf(getGameSettings().keyBindings, binding);

    if(i != -1) {
      getGameSettings().keyBindings = ArrayUtils.remove(getGameSettings().keyBindings, i);
      KeyBinding.resetKeyBindingArrayAndHash();
      return true;
    }

    return false;
  }
}
