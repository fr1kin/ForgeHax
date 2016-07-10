package com.matt.forgehax.util.container;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.matt.forgehax.ForgeHaxBase;

import java.io.*;
import java.util.Map;

/**
 * Used to store list/map data into json files easily
 */
public class ContainerList extends ForgeHaxBase {
    private final File file;
    // use linked hash map to retain order
    private final JsonObject root;

    public ContainerList(File file) {
        this.file = file;
        root = new JsonObject();
    }

    public File getFile() {
        return file;
    }

    /**
     * Add pair to map
     * Note: Will not update file
     */
    public <V extends JsonElement> void add(String keyName, V value) {
        root.add(keyName, value);
    }

    /**
     * Add primitives
     */
    public void add(String keyName, Integer value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Long value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Short value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Float value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Double value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Byte value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Boolean value) {
        add(keyName, new JsonPrimitive(value));
    }
    public void add(String keyName, Character value) {
        add(keyName, new JsonPrimitive(value));
    }

    /**
     * Remove pair from map
     * Note: Will not update file
     */
    public void remove(String keyName) {
        root.remove(keyName);
    }

    /**
     * Get element from list
     */
    public JsonElement get(String keyName) {
        return root.get(keyName);
    }

    /**
     * Read over saved json file (if it exists)
     */
    public void read() {
        if(file.exists() &&
                file.isFile() &&
                file.length() > 0) {
            BufferedReader buffer = null;
            try {
                FileReader reader = new FileReader(file);
                buffer = new BufferedReader(reader);
                try {
                    parseJsonObject(new JsonParser().parse(buffer).getAsJsonObject());
                } catch (JsonParseException e) {
                    MOD.getLog().error(String.format("Failed to read file %s: %s", file.getName(), e.getMessage()));
                }
            } catch(IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (buffer != null)
                        buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Save current json data
     */
    public void save() {
        try {
            FileWriter writer = new FileWriter(file);
            BufferedWriter buffer = new BufferedWriter(writer);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            buffer.write(gson.toJson(toJsonObject()));
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse over JsonObject and put data into map
     */
    private void parseJsonObject(JsonObject root) {
        for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Convert map data to json object
     */
    private JsonObject toJsonObject(JsonObject root) {
        for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
            root.add(entry.getKey(), entry.getValue());
        }
        return root;
    }
    private JsonObject toJsonObject() {
        return toJsonObject(new JsonObject());
    }
}
