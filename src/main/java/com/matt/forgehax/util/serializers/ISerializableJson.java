package com.matt.forgehax.util.serializers;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created on 5/20/2017 by fr1kin
 */
public interface ISerializableJson {
    // TODO: Change it so head = JsonObject to add to
    void serialize(final JsonWriter writer) throws IOException;
    void deserialize(final JsonReader reader) throws IOException;

    String toString();
}
