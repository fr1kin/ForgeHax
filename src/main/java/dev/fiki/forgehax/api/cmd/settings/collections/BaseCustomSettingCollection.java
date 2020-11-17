package dev.fiki.forgehax.api.cmd.settings.collections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.fiki.forgehax.api.cmd.AbstractSettingCollection;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

class BaseCustomSettingCollection<E extends IJsonSerializable, L extends Collection<E>>
    extends AbstractSettingCollection<E, L> {
  @Getter(AccessLevel.PROTECTED)
  private final Supplier<E> valueSupplier;

  public BaseCustomSettingCollection(IParentCommand parent,
      String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      Supplier<L> supplier, Collection<E> defaultTo,
      @NonNull Supplier<E> valueSupplier,
      List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags, supplier, defaultTo, listeners);
    this.valueSupplier = valueSupplier;
  }

  @Override
  public JsonElement serialize() {
    JsonArray array = new JsonArray();

    for(E obj : this) {
      array.add(obj.serialize());
    }

    return array;
  }

  @Override
  public void deserialize(JsonElement json) {
    JsonArray array = json.getAsJsonArray();

    // clear all children
    this.wrapping.clear();

    for(JsonElement element : array) {
      E value = getValueSupplier().get();
      value.deserialize(element);

      this.wrapping.add(value);
    }
  }
}
