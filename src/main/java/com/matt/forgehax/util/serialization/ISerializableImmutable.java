package com.matt.forgehax.util.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import javax.annotation.Nullable;

public interface ISerializableImmutable<E> {
  
  void serialize(JsonWriter writer, @Nullable E instance) throws IOException;

  @Nullable
  E deserialize(JsonReader reader) throws IOException;
}
