package dev.fiki.forgehax.api.cmd.settings.collections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.fiki.forgehax.api.cmd.AbstractSettingCollection;
import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.cmd.value.IValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class BaseSimpleSettingCollection<E, L extends Collection<E>> extends AbstractSettingCollection<E, L> {
  @Getter(AccessLevel.PROTECTED)
  private final IArgument<E> converterArgument;

  public BaseSimpleSettingCollection(IParentCommand parent, String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      Supplier<L> supplier, Collection<E> defaultTo,
      @NonNull IArgument<E> argument,
      List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags, supplier, defaultTo, listeners);
    this.converterArgument = argument;

    newSimpleCommand()
        .name("add")
        .description("Adds an element to the collection")
        .argument(argument)
        .executor(args -> {
          IValue<E> arg = args.getFirst();
          if (this.add(arg.getValue())) {
            args.inform("Added \"%s\" to the collection.", arg.getStringValue());
          } else {
            args.warn("Could not add \"%s\" to the collection (possible duplicate?).", arg.getStringValue());
          }
        })
        .build();

    newSimpleCommand()
        .name("remove")
        .alias("delete")
        .description("Removes an element to the collection")
        .argument(argument)
        .executor(args -> {
          IValue<E> arg = args.getFirst();
          if (this.remove(arg.getValue())) {
            args.inform("Removed \"%s\" from the collection.", arg.getStringValue());
          } else {
            args.warn("Could not add \"%s\" to the collection (possible duplicate?).", arg.getStringValue());
          }
        })
        .build();

    newSimpleCommand()
        .name("list")
        .alias("show")
        .alias("display")
        .description("Lists all the elements in the collection")
        .executor(args -> {
          if (this.isEmpty()) {
            args.inform("Collection is empty.");
          } else {
            args.inform(this.stream()
                .map(argument::convert)
                .collect(Collectors.joining(", ")));
          }
        })
        .build();

    newSimpleCommand()
        .name("clear")
        .description("Clear all the elements in the collection")
        .executor(args -> {
          int size = this.size();
          this.clear();
          args.inform("Cleared %d elements from the collection.", size);
        })
        .build();
  }

  @Override
  public JsonElement serialize() {
    JsonArray array = new JsonArray();

    IArgument<E> converter = getConverterArgument();

    for (E obj : this) {
      array.add(converter.convert(obj));
    }

    return array;
  }

  @Override
  public void deserialize(JsonElement json) {
    if (!json.isJsonArray()) {
      throw new IllegalArgumentException("expected JsonArray, got " + json.getClass().getSimpleName());
    }

    // clear all current children
    this.wrapping.clear();

    IArgument<E> converter = getConverterArgument();

    for (JsonElement element : json.getAsJsonArray()) {
      this.wrapping.add(converter.parse(element.getAsString()));
    }
  }

  @Override
  protected String printableValue(E o) {
    return getConverterArgument().print(o);
  }
}
