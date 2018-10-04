package com.matt.forgehax.util.command.v2.argument;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.converter.IConverter;
import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;
import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class BaseArg<E> implements IArg<E> {
  private final String description;
  private final String shortDescription;
  private final boolean required;
  private final E defaultValue;
  private final IConverter<E> converter;

  public BaseArg(
      String description,
      String shortDescription,
      boolean required,
      @Nullable E defaultValue,
      IConverter<E> converter) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(shortDescription);
    Objects.requireNonNull(converter);
    this.description = description;
    this.shortDescription = shortDescription;
    this.required = required;
    this.defaultValue = defaultValue;
    this.converter = converter;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getShortDescription() {
    return shortDescription;
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public boolean isOptional() {
    return !isRequired();
  }

  @Nullable
  @Override
  public E getDefaultValue() {
    return defaultValue;
  }

  @Override
  public Class<E> getType() {
    return converter.getType();
  }

  @Nullable
  @Override
  public Class<?> getPrimitiveType() {
    return converter.getPrimitiveType();
  }

  @Override
  public String getLabel() {
    return converter.getLabel();
  }

  @Override
  public String toString(E value) throws StringConversionException {
    return converter.toString(value);
  }

  @Nullable
  @Override
  public E valueOf(String input, Stream<E> stream) throws ValueParseException {
    return converter.valueOf(input, stream);
  }

  @Override
  public Stream<E> valuesOf(String input, Stream<E> stream) throws ValueParseException {
    return converter.valuesOf(input, stream);
  }

  @Override
  public int compare(E o1, E o2) {
    return converter.compare(o1, o2);
  }

  @Override
  public void serialize(JsonWriter writer, @Nullable E instance) throws IOException {
    converter.serialize(writer, instance);
  }

  @Nullable
  @Override
  public E deserialize(JsonReader reader) throws IOException {
    return converter.deserialize(reader);
  }
}
