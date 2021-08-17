package dev.fiki.forgehax.api.key;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.reflection.ReflectionTools;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static dev.fiki.forgehax.main.Common.getGameSettings;
import static dev.fiki.forgehax.main.Common.requiresMainThreadExecution;

public class BindingHelper {
  private static final Set<BindHandle> KEYS_HANDLES = Sets.newHashSet();

  @Getter
  private static boolean suppressingSettingsPacket = false;

  static {
    // cause key input class to load
    InputMappings.Type.KEYSYM.name();
    InputMappings.Type.MOUSE.name();
  }

  private static BindHandle getBindHandle(KeyBinding key) {
    synchronized (KEYS_HANDLES) {
      for (BindHandle handle : KEYS_HANDLES) {
        if (handle.getKey() == key) {
          return handle;
        }
      }
    }
    return null;
  }

  public static void restoreContextHandlers(Collection<KeyBinding> keys) {
    for (KeyBinding key : keys) {
      BindHandle handle = getBindHandle(key);
      if (handle != null) {
        handle.restoreContext();
        // not being used anymore
        if (handle.isRestored()) {
          synchronized (KEYS_HANDLES) {
            KEYS_HANDLES.remove(handle);
          }
        }
      }
    }
  }

  public static void restoreContextHandlers(KeyBinding... keys) {
    restoreContextHandlers(Arrays.asList(keys));
  }

  public static void restoreContextHandler(KeyBinding key) {
    restoreContextHandlers(Collections.singleton(key));
  }

  public static void disableContextHandlers(Collection<KeyBinding> keys) {
    for (KeyBinding key : keys) {
      BindHandle handle = getBindHandle(key);

      // create a new handle if one does not exist
      if (handle == null) {
        handle = new BindHandle(key);
        synchronized (KEYS_HANDLES) {
          KEYS_HANDLES.add(handle);
        }
      }

      // replace the current context handler
      handle.disableContext();
    }
  }

  public static void disableContextHandlers(KeyBinding... keys) {
    disableContextHandlers(Arrays.asList(keys));
  }

  public static void disableContextHandler(KeyBinding key) {
    disableContextHandlers(Collections.singleton(key));
  }

  private static String trimInputKeyName(InputMappings.Input input) {
    int len;
    if (!InputMappings.Type.MOUSE.equals(input.getType())) {
      len = (input.getType().name() + ".").length(); // TODO: 1.16 getName() returned something else
    } else {
      len = "key.".length();
    }
    return input.getName().substring(len);
  }

  public static InputMappings.Input getInputByName(String name) {
    return ReflectionTools.getInstance().InputMappings_Input_NAME_MAP.get(null)
        .values()
        .stream()
        .filter(input -> input.getName().equalsIgnoreCase(name)
            || trimInputKeyName(input).equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new Error("Unknown key: " + name));
  }

  public static InputMappings.Input getInputByKeyCode(int keyCode) {
    return ReflectionTools.getInstance().InputMappings_Input_NAME_MAP.get(null)
        .values()
        .stream()
        .filter(input -> input.getValue() == keyCode)
        .findFirst()
        .orElse(getInputUnknown());
  }

  public static InputMappings.Input getInputUnknown() {
    return InputMappings.UNKNOWN;
  }

  public static boolean isInputUnknown(InputMappings.Input input) {
    return getInputUnknown().equals(input);
  }

  public static IKeyConflictContext getEmptyKeyConflictContext() {
    return KeyConflictContexts.none();
  }

  public static void addBinding(KeyBinding binding) {
    requiresMainThreadExecution();

    ClientRegistry.registerKeyBinding(binding);
    updateKeyBindings();
  }

  public static boolean removeBinding(KeyBinding binding) {
    requiresMainThreadExecution();

    int i = ArrayUtils.indexOf(getGameSettings().keyMappings, binding);

    if (i != -1) {
      getGameSettings().keyMappings = ArrayUtils.remove(getGameSettings().keyMappings, i);
      updateKeyBindings();
      return true;
    }

    return false;
  }

  public static void updateKeyBindings() {
    requiresMainThreadExecution();
    KeyBinding.resetMapping();
  }

  public static void saveGameSettings() {
    if (getGameSettings() != null) {
      suppressingSettingsPacket = true;
      try {
        getGameSettings().save();
      } finally {
        suppressingSettingsPacket = false;
      }
    }
  }

  public static KeyBinding getKeyBindByDescription(String desc) {
    for (KeyBinding kb : getGameSettings().keyMappings) {
      if (kb.getName().equalsIgnoreCase(desc)) {
        return kb;
      }
    }
    return null;
  }
}
