package com.matt.forgehax.util.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nullable;
import java.io.IOException;

public interface ISerializableImmutable<E> {
    void serialize(JsonWriter writer, @Nullable E instance) throws IOException;

    @Nullable
    E deserialize(JsonReader reader) throws IOException;
}
