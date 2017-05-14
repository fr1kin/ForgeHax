package com.matt.forgehax.util.blocks;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.matt.forgehax.Wrapper;
import net.minecraft.block.state.IBlockState;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

/**
 * Created on 5/13/2017 by fr1kin
 */
public class BlockOptions {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final File file;

    private final Set<BlockEntry> entries = Sets.newConcurrentHashSet();

    public BlockOptions(File file) {
        this.file = file;
    }

    public BlockEntry getBlockEntry(IBlockState state) {
        for(BlockEntry entry : entries) if(entry.matches(state))
            return entry;
        return null;
    }

    public void read() {
        try {
            entries.clear();
            if (file.exists()) {
                JsonParser parser = new JsonParser();
                JsonObject head = parser.parse(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
                head.entrySet().forEach(entry -> {
                    try {
                        String name = entry.getKey();
                        JsonObject contents = entry.getValue().getAsJsonObject();
                        BlockEntry blockEntry = new BlockEntry(name);
                        if (blockEntry.getBlock() != null) {
                            blockEntry.read(contents);
                            entries.add(blockEntry);
                        }
                    } catch (Exception e) {
                        ;
                    }
                });
            } else {
                JsonObject root = new JsonObject();
                JsonObject contents = new JsonObject();
                contents.addProperty("r", 255);
                contents.addProperty("g", 255);
                contents.addProperty("b", 255);
                contents.addProperty("a", 255);
                root.add("minecraft:block_name::optional_metadata_id", contents);
                Files.write(file.toPath(), gson.toJson(root).getBytes());
            }
        } catch (Exception e) {
            Wrapper.getMod().printStackTrace(e);
        }
    }

    public void write() {
        // not needed atm
    }
}
