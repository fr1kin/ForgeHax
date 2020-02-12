package dev.fiki.forgehax.main.util.typeconverter.types;

import dev.fiki.forgehax.main.util.key.KeyInput;
import dev.fiki.forgehax.main.util.key.KeyInputs;
import dev.fiki.forgehax.main.util.typeconverter.TypeConverter;

public class KeyInputType extends TypeConverter<KeyInput> {
  @Override
  public String label() {
    return "keyinput";
  }

  @Override
  public Class<KeyInput> type() {
    return KeyInput.class;
  }

  @Override
  public KeyInput parse(String value) {
    return KeyInput.getKeyInputByName(value);
  }

  @Override
  public String convert(KeyInput value) {
    return value == null ? KeyInputs.UNBOUND.getName() : value.getName();
  }
}
