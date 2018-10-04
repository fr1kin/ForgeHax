package com.matt.forgehax.util.command.v2.argument;

import com.google.common.collect.ImmutableList;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;
import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class BaseOption<E> implements IOption<E> {
  private final List<String> names;
  private final boolean flag;
  private final String description;
  private final IArg<E> argument;

  public BaseOption(Collection<String> names, boolean flag, String description, IArg<E> argument) {
    Objects.requireNonNull(names);
    Objects.requireNonNull(description);
    Objects.requireNonNull(argument);
    this.names = ImmutableList.copyOf(names);
    this.flag = flag;
    this.description = description;
    this.argument = argument;
  }

  @Override
  public Collection<String> getNames() {
    return names;
  }

  @Override
  public String getOptionDescription() {
    return description;
  }

  @Override
  public String getDescription() {
    return argument.getDescription();
  }

  @Override
  public String getShortDescription() {
    return argument.getShortDescription();
  }

  @Override
  public boolean isRequired() {
    return !flag && argument.isRequired();
  }

  @Override
  public boolean isOptional() {
    return !flag && argument.isOptional();
  }

  @Nullable
  @Override
  public E getDefaultValue() {
    return argument.getDefaultValue();
  }

  @Override
  public Class<E> getType() {
    return argument.getType();
  }

  @Override
  public String getLabel() {
    return argument.getLabel();
  }

  @Override
  public String toString(E value) throws StringConversionException {
    return argument.toString(value);
  }

  @Override
  public E valueOf(String input, Stream<E> stream) throws ValueParseException {
    return argument.valueOf(input, stream);
  }

  @Override
  public Stream<E> valuesOf(String input, Stream<E> stream) throws ValueParseException {
    return argument.valuesOf(input, stream);
  }

  @Override
  public void serialize(JsonWriter writer, @Nullable E instance) throws IOException {
    argument.serialize(writer, instance);
  }

  @Nullable
  @Override
  public E deserialize(JsonReader reader) throws IOException {
    return argument.deserialize(reader);
  }

  @Override
  public int compare(E o1, E o2) {
    return argument.compare(o1, o2);
  }
}
