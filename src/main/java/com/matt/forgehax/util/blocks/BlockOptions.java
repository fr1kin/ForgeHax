package com.matt.forgehax.util.blocks;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.blocks.exceptions.BlockDoesNotExistException;
import com.matt.forgehax.util.json.GsonConstant;
import net.minecraft.block.Block;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;
import java.util.function.Consumer;

import static com.matt.forgehax.Wrapper.printStackTrace;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class BlockOptions implements GsonConstant {
    private final File file;

    private final Set<AbstractBlockEntry> entries = Sets.newConcurrentHashSet();

    public BlockOptions(File file) {
        this.file = file;
    }

    public boolean add(AbstractBlockEntry entry) {
        return entry != null && entries.add(entry);
    }

    public boolean remove(AbstractBlockEntry entry) {
        return entry != null && entries.remove(entry);
    }

    public AbstractBlockEntry get(Block block, int metadataId) {
        for(AbstractBlockEntry entry : entries) if(entry.isEqual(block, metadataId))
            return entry;
        return null;
    }

    public void deserialize() {
        try {
            entries.clear();
            if (file.exists()) {
                JsonParser parser = new JsonParser();
                final JsonObject head = parser.parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
                head.entrySet().forEach(entry -> {
                    AbstractBlockEntry blockEntry = null;
                    try {
                        blockEntry = BlockEntry.createByUniqueName(entry.getKey());
                    } catch (BlockDoesNotExistException e) {
                        try {
                            blockEntry = InvalidBlockEntry.createByUniqueName(entry.getKey());
                        } catch (Exception ee) {
                            blockEntry = null;
                        }
                    } catch (Exception e) {
                        blockEntry = null;
                    } finally {
                        if(blockEntry != null) try {
                            blockEntry.deserialize(head);
                            add(blockEntry);
                        } catch (Exception e) {
                            ;
                        }
                    }
                });
            } else {
                Files.write(file.toPath(), GSON_PRETTY.toJson(new JsonObject()).getBytes());
            }
        } catch (Exception e) {
            Wrapper.printStackTrace(e);
        }
    }

    public void serialize() {
        try {
            final JsonObject root = new JsonObject();
            entries.forEach(entry -> entry.serialize(root));
            Files.write(file.toPath(), GSON_PRETTY.toJson(root).getBytes());
        } catch (Exception e) {
            Wrapper.printStackTrace(e);
        }
    }

    public void forEach(Consumer<AbstractBlockEntry> consumer) {
        entries.forEach(consumer);
    }
}
