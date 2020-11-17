package dev.fiki.forgehax.api.serialization;

import com.google.gson.JsonElement;

public interface IJsonSerializable {
  JsonElement serialize();
  void deserialize(JsonElement json);
}
