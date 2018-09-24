package com.matt.forgehax.util.command.v2.converter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;
import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import com.matt.forgehax.util.serialization.ISerializableImmutable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public class BaseConverter<E> extends AbstractConverter<E> {
    private final Class<E> type;
    private final Class<?> primitiveType;
    private final String label;
    private final Comparator<E> comparator;
    private final ValuesConverter<E> valuesConverter;
    private final ValueConverter<E> valueConverter;
    private final StringConverter<E> stringConverter;
    private final ISerializableImmutable<E> serializer;

    public BaseConverter(Class<E> type,
                         Class<?> primitiveType,
                         String label,
                         Comparator<E> comparator,
                         ValuesConverter<E> valuesConverter,
                         ValueConverter<E> valueConverter,
                         StringConverter<E> stringConverter,
                         ISerializableImmutable<E> serializer) {
        Objects.requireNonNull(type, "missing type");
        Objects.requireNonNull(label, "missing label");
        Objects.requireNonNull(comparator, "missing comparator");
        Objects.requireNonNull(valuesConverter, "missing values converter");
        Objects.requireNonNull(valueConverter, "missing value converter");
        Objects.requireNonNull(stringConverter, "missing string converter");
        Objects.requireNonNull(serializer, "missing serializer");

        this.type = type;
        this.primitiveType = primitiveType;
        this.label = label;
        this.comparator = comparator;
        this.valuesConverter = valuesConverter;
        this.valueConverter = valueConverter;
        this.stringConverter = stringConverter;
        this.serializer = serializer;
    }

    @Override
    public Class<E> getType() {
        return type;
    }

    @Nullable
    @Override
    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Builder<E> builder() {
        return ConverterFactory.<E>newBuilder() // copy the values directly to reduce memory overhead
                .type(type)
                .primitiveType(primitiveType)
                .label(label)
                .comparator(comparator)
                .valuesOf(valuesConverter)
                .valueOf(valueConverter)
                .toString(stringConverter)
                .serializer(serializer);
    }

    @Override
    public Stream<E> valuesOf(String input, Stream<E> stream) throws ValueParseException {
        return valuesConverter.valuesOf(input, stream);
    }

    @Nullable
    @Override
    public E valueOf(String input, Stream<E> stream) throws ValueParseException {
        return valueConverter.valueOf(input, stream);
    }

    @Override
    public String toString(E value) throws StringConversionException {
        return stringConverter.toString(value);
    }

    @Override
    public int compare(E o1, E o2) {
        return comparator.compare(o1, o2);
    }

    @Override
    public void serialize(JsonWriter writer, E instance) throws IOException {
        serializer.serialize(writer, instance);
    }

    @Override
    public E deserialize(JsonReader reader) throws IOException {
        return serializer.deserialize(reader);
    }
}
