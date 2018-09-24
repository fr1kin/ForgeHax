package com.matt.forgehax.util.command.v2.argument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;
import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgMap<E> implements IArg<E>, ValueMap<E> {
    private final IArg<E> parent;
    private final List<String> inputs = Lists.newArrayList();

    ArgMap(IArg<E> parent) {
        Objects.requireNonNull(parent);
        this.parent = parent;
    }

    @Override
    public String getDescription() {
        return parent.getDescription();
    }

    @Override
    public String getShortDescription() {
        return parent.getShortDescription();
    }

    @Override
    public boolean isRequired() {
        return parent.isRequired();
    }

    @Override
    public boolean isOptional() {
        return parent.isOptional();
    }

    @Nullable
    @Override
    public E getDefaultValue() {
        return parent.getDefaultValue();
    }

    @Override
    public Class<E> getType() {
        return parent.getType();
    }

    @Override
    public String getLabel() {
        return parent.getLabel();
    }

    @Override
    public String toString(E value) throws StringConversionException {
        return parent.toString(value);
    }

    @Override
    public E valueOf(String input, Stream<E> stream) throws ValueParseException {
        return parent.valueOf(input, stream);
    }

    @Override
    public Stream<E> valuesOf(String input, Stream<E> stream) throws ValueParseException {
        return parent.valuesOf(input, stream);
    }

    @Override
    public void serialize(JsonWriter writer, @Nullable E instance) throws IOException {
        parent.serialize(writer, instance);
    }

    @Nullable
    @Override
    public E deserialize(JsonReader reader) throws IOException {
        return parent.deserialize(reader);
    }

    @Override
    public int compare(E o1, E o2) {
        return parent.compare(o1, o2);
    }

    @Override
    public Stream<E> values(int index, Stream<E> filter) {
        return valuesOf(inputs.get(index), filter);
    }

    @Override
    public Collection<String> getInputs() {
        return inputs;
    }

    @Override
    public int count() {
        return inputs.size();
    }

    @Override
    public void withInputs(Iterable<String> inputs) {
        for(String s : inputs) this.inputs.add(s);
    }

    @Override
    public void withDefaultValue() {
        withInput(toString(getDefaultValue()));
    }
}
