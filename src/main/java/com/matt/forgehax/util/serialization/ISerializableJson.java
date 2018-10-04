package com.matt.forgehax.util.serialization;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/** Created on 5/20/2017 by fr1kin */
public interface ISerializableJson {
  /**
   * Serialize this object and all necessary data into json
   *
   * @param writer json writer
   * @throws IOException if you format the json incorrectly
   */
  void serialize(final JsonWriter writer) throws IOException;

  /**
   * Deserialize data from json into a new object.
   *
   * @param reader json reader
   * @throws IOException if you read the json incorrectly
   */
  void deserialize(final JsonReader reader) throws IOException;

  /**
   * A unique heading to identify this object.
   *
   * @return unique name
   */
  default String getUniqueHeader() {
    return toString();
  }
}
