package com.matt.forgehax.util.entry;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.serialization.ISerializableJson;

import java.io.IOException;

/**
 * Created on 7/21/2017 by fr1kin
 */
public class CustomMessageEntry implements ISerializableJson {
    private final String player;

    private String message;
    private String setter;

    public CustomMessageEntry(String name) {
        this.player = name;
    }

    /**
     * The player this join message is for
     * @return
     */
    public String getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    /**
     * The ID of the person this join message is set for
     * @return
     */
    public String getSetter() {
        return setter;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSetterId(String setter) {
        this.setter = setter;
    }

    @Override
    public void serialize(JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name("message");
        writer.value(message);

        writer.name("setterId");
        writer.value(setter);

        writer.endObject();
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
        reader.beginObject();

        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "message":
                    setMessage(reader.nextString());
                    break;
                case "setterId":
                    setSetterId(reader.nextString());
                    break;
                default: break;
            }
        }

        reader.endObject();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this
                || (obj instanceof CustomMessageEntry && player.equalsIgnoreCase(((CustomMessageEntry) obj).getPlayer()))
                || (obj instanceof String && player.equalsIgnoreCase((String) obj));
    }

    @Override
    public int hashCode() {
        return player.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return player;
    }
}
