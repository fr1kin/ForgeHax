package dev.fiki.forgehax.main.util.serialization;

import com.google.gson.JsonElement;

public interface IJsonSerializable {
  JsonElement serialize();
  void deserialize(JsonElement json);
}
