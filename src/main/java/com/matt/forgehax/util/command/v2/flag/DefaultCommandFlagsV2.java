package com.matt.forgehax.util.command.v2.flag;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created on 12/26/2017 by fr1kin
 */
public enum DefaultCommandFlagsV2 implements ICommandFlagV2 {
    HIDDEN,
    ;

    @Override
    public void serialize(JsonWriter writer) throws IOException {
        writer.value(getDeclaringClass().getName() + "::" + name());
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
        throw new UnsupportedOperationException("deserialization not supported");
    }

    static {
        Registry.register(DefaultCommandFlagsV2.class);
    }
}
