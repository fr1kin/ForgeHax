package com.matt.forgehax.util.container;

import com.google.gson.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

/**
 * Used to store list/map data into json files easily
 */
public class ContainerList implements Globals {
    private final File file;
    private final String name;
    // use linked hash map to retain order
    private JsonObject root;

    public ContainerList(String name, File file) {
        this.name = name;
        this.file = file;
        root = new JsonObject();
    }

    public String getName() {
        return name;
    }

    protected File getFile() {
        return file;
    }

    /**
     * Add pair to map
     * Note: Will not update file
     */
    protected <V extends JsonElement> void add(String keyName, V value) {
        root.add(keyName, value);
    }

    /**
     * Add primitives
     */
    protected void add(String keyName, Integer value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Long value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Short value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, String value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Float value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Double value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Byte value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Boolean value) {
        add(keyName, new JsonPrimitive(value));
    }
    protected void add(String keyName, Character value) {
        add(keyName, new JsonPrimitive(value));
    }

    /**
     * Remove pair from map
     * Note: Will not update file
     */
    protected boolean remove(String keyName) {
        return root.remove(keyName) != null;
    }

    /**
     * Get element from list
     */
    protected JsonElement get(String keyName) {
        return root.get(keyName);
    }

    /**
     * Check if element is in the list
     */
    protected boolean contains(String keyName) {
        return root.has(keyName);
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return root.entrySet();
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
                    root = new JsonObject();
                    parseJsonObject(new JsonParser().parse(buffer).getAsJsonObject());
                } catch (JsonParseException e) {
                    LOGGER.error(String.format("Failed to read file %s: %s", file.getName(), e.getMessage()));
                }
            } catch(IOException e) {
                Wrapper.printStackTrace(e);
            } finally {
                try {
                    if (buffer != null)
                        buffer.close();
                } catch (IOException e) {
                    Wrapper.printStackTrace(e);
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
            buffer.write(gson.toJson(root));
            buffer.close();
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
        }
    }

    /**
     * Deletes this file
     */
    public boolean delete() {
        boolean deletedFile = false;
        try {
            deletedFile = Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
        }
        return deletedFile;
    }

    /**
     * Size of the
     */
    public int size() {
        return root.entrySet().size();
    }

    /**
     * Parse over JsonObject and put data into map
     */
    protected void parseJsonObject(JsonObject root) {
        for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Convert map data to json object
     */
    protected JsonObject toJsonObject(JsonObject json) {
        for(Map.Entry<String, JsonElement> entry : root.entrySet()) {
            json.add(entry.getKey(), entry.getValue());
        }
        return json;
    }
    protected JsonObject toJsonObject() {
        return toJsonObject(new JsonObject());
    }
}
