package com.matt.forgehax.util.command;

import com.matt.forgehax.Globals;
import com.matt.forgehax.util.command.callbacks.OnChangeCallback;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverterRegistry;
import java.util.Comparator;
import java.util.function.Consumer;

/** Created on 6/3/2017 by fr1kin */
public class SettingBuilder<E> extends BaseCommandBuilder<SettingBuilder<E>, Setting<E>>
    implements Globals {
  public SettingBuilder<E> changed(Consumer<OnChangeCallback<E>> consumer) {
    getCallbacks(CallbackType.CHANGE).add(consumer);
    return this;
  }

  public SettingBuilder<E> defaultTo(E defaultValue) {
    return insert(Setting.DEFAULTVALUE, defaultValue).type(defaultValue.getClass());
  }

  public SettingBuilder<E> converter(TypeConverter<E> converter) {
    return insert(Setting.CONVERTER, converter).comparator(converter.comparator());
  }

  public SettingBuilder<E> comparator(Comparator<E> comparator) {
    return insert(Setting.COMPARATOR, comparator);
  }

  public SettingBuilder<E> min(E minValue) {
    return insert(Setting.MINVALUE, minValue);
  }

  public SettingBuilder<E> max(E maxValue) {
    return insert(Setting.MAXVALUE, maxValue);
  }

  public SettingBuilder<E> type(Class<?> clazz) {
    if (has(Setting.CONVERTER)) return this;
    return converter(TypeConverterRegistry.get(clazz));
  }

  public SettingBuilder<E> customProcessor() {
    return insert(Setting.DEFAULTPROCESSOR, false);
  }

  @Override
  public Setting<E> build() {
    return new Setting<>(has(Command.REQUIREDARGS) ? data : requiredArgs(1).data);
  }
}
