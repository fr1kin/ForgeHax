package com.matt.forgehax.util.command.v2.converter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public abstract class AbstractConverter<E> implements IConverter<E> {
    @Override
    public void serialize(JsonWriter writer, @Nullable E instance) throws IOException {
        if(instance == null)
            writer.nullValue();
        else
            writer.value(toString(instance));
    }

    @Nullable
    @Override
   public E deserialize(JsonReader reader) throws IOException {
        if(reader.peek() == JsonToken.NULL) {
            reader.skipValue();
            return null;
        } else
            return valueOf(reader.nextString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getPrimitiveType());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof IConverter && Objects.equals(getType(), ((IConverter) obj).getType()) && Objects.equals(getPrimitiveType(), ((IConverter) obj).getPrimitiveType()));
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
