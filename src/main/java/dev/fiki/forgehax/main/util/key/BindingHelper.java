package dev.fiki.forgehax.main.util.key;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;

public class BindingHelper {
  
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

  private static String trimInputKeyName(InputMappings.Input input) {
    int len;
    if(!InputMappings.Type.MOUSE.equals(input.getType())) {
      len = (input.getType().getName() + ".").length();
    } else {
      len = "key.".length();
    }
    return input.getTranslationKey().substring(len);
  }
  
//  public static String getIndexName(int code) {
//    return Arrays.stream(InputMappings.Type.values())
//        .map(type -> type.getOrMakeInput(code))
//        .filter(Objects::nonNull)
//        .limit(1)
//        .findAny()
//        .map(InputMappings.Input::getTranslationKey)
//        .orElse("unknown");
//  }
  
  public static InputMappings.Input getInputByName(String name) {
    return FastReflection.Fields.InputMappings_REGISTRY.get(null)
        .values()
        .stream()
        .filter(input -> input.getTranslationKey().equalsIgnoreCase(name)
            || trimInputKeyName(input).equalsIgnoreCase(name))
        .findAny()
        .orElseThrow(() -> new Error("Unknown key: " + name));
  }

  public static InputMappings.Input getInputByKeyCode(int keyCode) {
    return FastReflection.Fields.InputMappings_REGISTRY.get(null)
        .values()
        .stream()
        .filter(input -> input.getKeyCode() == keyCode)
        .findAny()
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
    ClientRegistry.registerKeyBinding(binding);
    KeyBinding.resetKeyBindingArrayAndHash();
  }

  public static boolean removeBinding(KeyBinding binding) {
    int i = ArrayUtils.indexOf(Common.getGameSettings().keyBindings, binding);

    if(i != -1) {
      Common.getGameSettings().keyBindings = ArrayUtils.remove(Common.getGameSettings().keyBindings, i);
      KeyBinding.resetKeyBindingArrayAndHash();
      return true;
    }

    return false;
  }

  public static KeyBinding getKeyBindByDescription(String desc) {
    for(KeyBinding kb : Common.getGameSettings().keyBindings) {
      if(kb.getKeyDescription().equalsIgnoreCase(desc)) {
        return kb;
      }
    }
    return null;
  }
}
