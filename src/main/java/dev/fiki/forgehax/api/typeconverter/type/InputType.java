package dev.fiki.forgehax.api.typeconverter.type;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.api.key.BindingHelper;
import dev.fiki.forgehax.api.typeconverter.TypeConverter;
import net.minecraft.client.util.InputMappings;

public class InputType extends TypeConverter<InputMappings.Input> {
  @Override
  public String label() {
    return "mcinput";
  }

  @Override
  public Class<InputMappings.Input> type() {
    return InputMappings.Input.class;
  }

  @Override
  public InputMappings.Input parse(String value) {
    return "none".equalsIgnoreCase(value)
        ? InputMappings.UNKNOWN
        : BindingHelper.getInputByName(value.toLowerCase());
  }

  @Override
  public String convert(InputMappings.Input value) {
    return MoreObjects.firstNonNull(value, InputMappings.UNKNOWN).getName();
  }
}
