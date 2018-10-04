package com.matt.forgehax.util.key;

import static java.util.stream.Collectors.toList;

import com.matt.forgehax.Globals;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class Bindings implements Globals {

  public static final List<KeyBindingHandler> KEY_LIST = getAllKeys();

  @Nullable
  public static KeyBindingHandler getKey(String name) {
    return Bindings.KEY_LIST
        .stream()
        .filter(k -> k.getBinding().getKeyDescription().toLowerCase().contains(name.toLowerCase()))
        .findFirst()
        .orElse(null);
  }

  public static final KeyBindingHandler forward =
      new KeyBindingHandler(MC.gameSettings.keyBindForward);
  public static final KeyBindingHandler back = new KeyBindingHandler(MC.gameSettings.keyBindBack);
  public static final KeyBindingHandler left = new KeyBindingHandler(MC.gameSettings.keyBindLeft);
  public static final KeyBindingHandler right = new KeyBindingHandler(MC.gameSettings.keyBindRight);

  public static final KeyBindingHandler jump = new KeyBindingHandler(MC.gameSettings.keyBindJump);

  public static final KeyBindingHandler sprint =
      new KeyBindingHandler(MC.gameSettings.keyBindSprint);
  public static final KeyBindingHandler sneak = new KeyBindingHandler(MC.gameSettings.keyBindSneak);

  public static final KeyBindingHandler attack =
      new KeyBindingHandler(MC.gameSettings.keyBindAttack);
  public static final KeyBindingHandler use = new KeyBindingHandler(MC.gameSettings.keyBindUseItem);

  // reflectively get KeyBindingHandlers from GameSettings
  @Nullable
  private static List<KeyBindingHandler> getAllKeys() {
    Field[] fields = GameSettings.class.getFields();
    return Arrays.stream(fields)
        .filter(f -> f.getType() == KeyBinding.class)
        .map(Bindings::getBinding)
        .filter(Objects::nonNull)
        .map(KeyBindingHandler::new)
        .collect(toList());
  }

  private static KeyBinding getBinding(Field field) {
    try {
      return (KeyBinding) field.get(MC.gameSettings);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }
  }
}
