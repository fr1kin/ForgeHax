package dev.fiki.forgehax.api.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.fiki.forgehax.api.cmd.argument.ConverterArgument;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.execution.ArgumentList;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.typeconverter.IConverter;
import lombok.Getter;

import java.util.*;

@Getter
public abstract class AbstractSetting<E> extends AbstractCommand implements ISetting<E> {
  private final Multimap<Class<? extends ICommandListener>, ICommandListener> listeners =
      Multimaps.newListMultimap(Maps.newConcurrentMap(), Lists::newCopyOnWriteArrayList);

  private final E defaultValue;
  private final E minValue;
  private final E maxValue;
  private E value;

  public AbstractSetting(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags,
      E defaultTo, E min, E max) {
    super(parent, name, aliases, description, flags);
    this.defaultValue = defaultTo;
    this.value = defaultTo;
    this.minValue = min;
    this.maxValue = max;
  }

  protected abstract IConverter<E> getConverter();

  protected int getMinArguments() {
    return 1;
  }

  protected int getMaxArguments() {
    return 1;
  }

  @Override
  public boolean setValue(E value) {
    Comparator<E> comparator = getConverter().comparator();
    if (comparator != null
        && getValue() != null
        && value != null) {
      if (getMinValue() != null && comparator.compare(value, getMinValue()) < 0) {
        value = getMinValue();
      } else if (getMaxValue() != null && comparator.compare(value, getMaxValue()) > 0) {
        value = getMaxValue();
      }
    }

    // update only if no listener returns false and the value is different
    if ((comparator != null && comparator.compare(value, getValue()) != 0)
        || !Objects.equals(value, getValue())) {
      final E newValue = value;
      final E oldValue = this.value;
      this.value = value;
      invokeListeners(ISettingValueChanged.class, l -> l.onValueChanged(oldValue, newValue));
      callUpdateListeners();
      return true;
    }

    return false;
  }

  @Override
  public boolean setValueRaw(String value) {
    return setValue(getConverter().parse(value));
  }

  @Override
  public List<IArgument<?>> getArguments() {
    return Collections.singletonList(ConverterArgument.<E>builder()
        .converter(getConverter())
        .defaultValue(getDefaultValue())
        .minArgumentsConsumed(getMinArguments())
        .maxArgumentsConsumed(getMaxArguments())
        .build());
  }

  @Override
  public ICommand onExecute(ArgumentList args) {
    if (setValue(args.<E>getFirst().getValue())) {
      args.inform("%s = %s", getFullName(), args.<E>getFirst().getStringValue());
    }
    return null;
  }

  @Override
  public boolean addListeners(Class<? extends ICommandListener> type, Collection<? extends ICommandListener> listener) {
    return type != null
        && listener != null
        && listeners.putAll(type, listener);
  }

  @Override
  public <T extends ICommandListener> List<T> getListeners(Class<T> type) {
    return (List<T>) listeners.get(type);
  }

  @Override
  public JsonElement serialize() {
    return new JsonPrimitive(getConverter().convert(getValue()));
  }

  @Override
  public void deserialize(JsonElement json) {
    if (json.isJsonPrimitive()) {
      value = getConverter().parse(json.getAsString());
    } else {
      throw new IllegalArgumentException("expected JsonPrimitive, got " + json.getClass().getSimpleName());
    }
  }

  @Override
  public String toString() {
    return getName() + " = " + getConverter().convert(getValue());
  }
}
