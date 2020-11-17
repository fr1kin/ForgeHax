package dev.fiki.forgehax.api.typeconverter.type;

import dev.fiki.forgehax.api.key.KeyInput;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.typeconverter.TypeConverter;

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
