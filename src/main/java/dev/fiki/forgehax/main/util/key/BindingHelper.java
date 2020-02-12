package dev.fiki.forgehax.main.util.key;

import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static dev.fiki.forgehax.main.Common.*;

public class BindingHelper {

  static {
    // cause key input class to load
    InputMappings.Type.KEYSYM.getName();
    InputMappings.Type.MOUSE.getName();
  }

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

  public static KeysHandle disableContextHandlers(Collection<KeyBinding> keys) {
    return new KeysHandle(keys);
  }

  public static KeysHandle disableContextHandlers(KeyBinding... keys) {
    return disableContextHandlers(Arrays.asList(keys));
  }

  public static KeysHandle disableContextHandler(KeyBinding key) {
    return disableContextHandlers(Collections.singleton(key));
  }

  private static String trimInputKeyName(InputMappings.Input input) {
    int len;
    if (!InputMappings.Type.MOUSE.equals(input.getType())) {
      len = (input.getType().getName() + ".").length();
    } else {
      len = "key.".length();
    }
    return input.getTranslationKey().substring(len);
  }

  public static InputMappings.Input getInputByName(String name) {
    return FastReflection.Fields.InputMappings_REGISTRY.get(null)
        .values()
        .stream()
        .filter(input -> input.getTranslationKey().equalsIgnoreCase(name)
            || trimInputKeyName(input).equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new Error("Unknown key: " + name));
  }

  public static InputMappings.Input getInputByKeyCode(int keyCode) {
    return FastReflection.Fields.InputMappings_REGISTRY.get(null)
        .values()
        .stream()
        .filter(input -> input.getKeyCode() == keyCode)
        .findFirst()
        .orElse(getInputUnknown());
  }

  public static InputMappings.Input getInputUnknown() {
    return InputMappings.INPUT_INVALID;
  }

  public static boolean isInputUnknown(InputMappings.Input input) {
    return getInputUnknown().equals(input);
  }

  public static IKeyConflictContext getEmptyKeyConflictContext() {
    return EMPTY;
  }

  public static void addBinding(KeyBinding binding) {
    requiresMainThreadExecution();

    ClientRegistry.registerKeyBinding(binding);
    updateKeyBindings();
  }

  public static boolean removeBinding(KeyBinding binding) {
    requiresMainThreadExecution();

    int i = ArrayUtils.indexOf(getGameSettings().keyBindings, binding);

    if (i != -1) {
      getGameSettings().keyBindings = ArrayUtils.remove(getGameSettings().keyBindings, i);
      updateKeyBindings();
      return true;
    }

    return false;
  }

  public static void updateKeyBindings() {
    requiresMainThreadExecution();
    KeyBinding.resetKeyBindingArrayAndHash();
  }

  public static KeyBinding getKeyBindByDescription(String desc) {
    for (KeyBinding kb : getGameSettings().keyBindings) {
      if (kb.getKeyDescription().equalsIgnoreCase(desc)) {
        return kb;
      }
    }
    return null;
  }
}
