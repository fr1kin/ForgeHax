package com.matt.forgehax.util.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import javax.annotation.Nullable;

public class Serializers {
  private static final ISerializableImmutable IMMUTABLE_NULL =
      new ISerializableImmutable() {
        @Override
        public void serialize(JsonWriter writer, @Nullable Object instance) throws IOException {
          writer.nullValue();
        }

        @Nullable
        @Override
        public Object deserialize(JsonReader reader) throws IOException {
          reader.nextNull();
          return null;
        }
      };

  public static <T> ISerializableImmutable<T> nullSerializer() {
    return IMMUTABLE_NULL;
  }
}
