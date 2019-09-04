package com.matt.forgehax.util.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public interface ISerializableMutable<E> {
  
  void serialize(E instance, JsonWriter writer) throws IOException;

  void deserialize(E instance, JsonReader reader) throws IOException;

  default String heading() {
    return toString();
  }
}
