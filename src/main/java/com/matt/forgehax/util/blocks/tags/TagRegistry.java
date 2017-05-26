package com.matt.forgehax.util.blocks.tags;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.json.GsonConstant;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created on 5/23/2017 by fr1kin
 */
public class TagRegistry implements GsonConstant {
    private final File saveFile;

    private final Collection<BlockTag> tags = Sets.newConcurrentHashSet();

    public TagRegistry(File blockOptionsFile) {
        saveFile = new File(blockOptionsFile.getParentFile(), Files.getNameWithoutExtension(blockOptionsFile.getName()) + "_tags.json");
    }

    public File getSaveFile() {
        return saveFile;
    }

    public boolean add(BlockTag tag) {
        return tag != null && tags.add(tag);
    }

    public boolean remove(BlockTag tag) {
        return tag != null && tags.remove(tag);
    }

    public BlockTag get(String name) {
        for(BlockTag tag : tags) if(tag.getName().equalsIgnoreCase(name))
            return tag;
        return null;
    }

    public void serialize() {
        final JsonObject root = new JsonObject();
        tags.forEach(tag -> tag.serialize(root));

        try {
            java.nio.file.Files.write(saveFile.toPath(), GSON_PRETTY.toJson(root).getBytes());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
        }
    }

    public void deserialize() {
        tags.clear();
        if(saveFile.exists()) {
            try {
                JsonParser parser = new JsonParser();
                final JsonObject head = parser.parse(new String(java.nio.file.Files.readAllBytes(saveFile.toPath()))).getAsJsonObject();
                head.entrySet().forEach(entry -> {
                    BlockTag tag = new BlockTag(entry.getKey());
                    tag.deserialize(head);
                    add(tag);
                });
            } catch (IOException e) {
                Wrapper.printStackTrace(e);
            }
        }
    }
}
