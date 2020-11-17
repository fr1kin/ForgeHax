package dev.fiki.forgehax.api.cmd.settings.maps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fiki.forgehax.api.cmd.AbstractSettingMap;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.cmd.value.IValue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SimpleSettingMap<K, V> extends AbstractSettingMap<K, V, Map<K, V>> {
  @Getter(AccessLevel.PROTECTED)
  private final IArgument<K> keyArgumentConverter;
  @Getter(AccessLevel.PROTECTED)
  private final IArgument<V> valueArgumentConverter;

  @Builder
  public SimpleSettingMap(IParentCommand parent,
      String name, @Singular Collection<String> aliases, String description,
      @Singular Collection<EnumFlag> flags,
      Supplier<Map<K, V>> supplier,
      IArgument<K> keyArgument, IArgument<V> valueArgument,
      @Singular List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags, supplier, listeners);
    this.keyArgumentConverter = keyArgument;
    this.valueArgumentConverter = valueArgument;

    newSimpleCommand()
        .name("add")
        .alias("put")
        .description("Add key and value to the map")
        .argument(keyArgument)
        .argument(valueArgument)
        .executor(args -> {
          IValue<K> key = args.getFirst();
          IValue<V> value = args.getSecond();

          K k = key.getValue();
          V v = value.getValue();

          Objects.requireNonNull(k, key.getConverter().getLabel() + " argument is invalid or missing");
          Objects.requireNonNull(v, value.getConverter().getLabel() + " argument is invalid or missing");

          V previous = this.put(k, v);
          if (previous == null) {
            args.inform("Added entry \"%s\", \"%s\" to map",
                key.getStringValue(), value.getStringValue());
          } else {
            args.warn("Replaced previous value \"%s\" with \"%s\" for key \"%s\"",
                valueArgumentConverter.convert(previous),
                value.getStringValue(), key.getStringValue());
          }
        })
        .build();

    newSimpleCommand()
        .name("remove")
        .alias("delete")
        .description("Remove a key from the map")
        .argument(keyArgument)
        .executor(args -> {
          IValue<K> key = args.getFirst();

          K k = key.getValue();

          Objects.requireNonNull(k, key.getConverter().getLabel() + " argument is invalid or missing");

          V removed = this.remove(k);
          if (removed != null) {
            args.inform("Removed key \"%s\" from map", key.getStringValue());
          } else {
            args.warn("No key named \"%s\" found", key.getStringValue());
          }
        })
        .build();

    newSimpleCommand()
        .name("list")
        .alias("show")
        .alias("display")
        .description("List the contents of the map")
        .executor(args -> {
          if (this.isEmpty()) {
            args.inform("Map is empty.");
          } else {
            args.inform(this.entrySet().stream()
                .map(e -> keyArgumentConverter.convert(e.getKey())
                    + "=" + valueArgumentConverter.convert(e.getValue()))
                .collect(Collectors.joining(", ")));
          }
        })
        .build();

    newSimpleCommand()
        .name("clear")
        .description("Empties the map of all its contents")
        .executor(args -> {
          int size = this.size();
          this.clear();
          args.inform("Cleared %d entries from the map.", size);
        })
        .build();

    onFullyConstructed();
  }

  @Override
  public JsonElement serialize() {
    JsonObject root = new JsonObject();

    IArgument<K> keyConverter = getKeyArgumentConverter();
    IArgument<V> valueConverter = getValueArgumentConverter();

    for (Map.Entry<K, V> entry : this.entrySet()) {
      root.addProperty(keyConverter.convert(entry.getKey()), valueConverter.convert(entry.getValue()));
    }

    return root;
  }

  @Override
  public void deserialize(JsonElement json) {
    if (!json.isJsonObject()) {
      throw new IllegalArgumentException("expected JsonObject, got " + json.getClass().getSimpleName());
    }

    JsonObject object = json.getAsJsonObject();

    IArgument<K> keyConverter = getKeyArgumentConverter();
    IArgument<V> valueConverter = getValueArgumentConverter();

    for (Entry<String, JsonElement> element : object.entrySet()) {
      this.wrapping.put(keyConverter.parse(element.getKey()), valueConverter.parse(element.getValue().getAsString()));
    }
  }

  @Override
  protected String printableKey(K o) {
    return getKeyArgumentConverter().print(o);
  }

  @Override
  protected String printableValue(V o) {
    return getValueArgumentConverter().print(o);
  }
}
