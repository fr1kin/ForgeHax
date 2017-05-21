package com.matt.forgehax.util.json;

import com.google.gson.JsonObject;

/**
 * Created on 5/20/2017 by fr1kin
 */
public interface ISerializableJson {
    void serialize(final JsonObject head);
    void deserialize(final JsonObject head);
}
