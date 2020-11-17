package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;

public interface ISetting<E> extends ICommand, IJsonSerializable {
  E getValue();
  boolean setValue(E value);
  boolean setValueRaw(String value);

  E getDefaultValue();
  default boolean setValueToDefault() {
    return setValue(getDefaultValue());
  }

  E getMinValue();
  E getMaxValue();

  interface ISettingValueChanged<E> extends ICommandListener {
    void onValueChanged(E from, E to);

    @Override
    default Class<? extends ICommandListener> getListenerClassType() {
      return ISettingValueChanged.class;
    }
  }
}
