package com.matt.forgehax.util.blocks;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.matt.forgehax.Wrapper;
import net.minecraft.block.state.IBlockState;

import java.io.*;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

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

    public boolean addBlockEntry(BlockEntry entry) {
        return entries.add(entry);
    }

    public BlockEntry getBlockEntry(IBlockState state) {
        for(BlockEntry entry : entries) if(entry.matches(state))
            return entry;
        return null;
    }

    public BlockEntry getBlockEntry(BlockEntry replica) {
        for(BlockEntry entry : entries) if(Objects.equals(replica, entry))
            return entry;
        return null;
    }

    public boolean removeBlockEntry(BlockEntry replica) {
        BlockEntry remove = getBlockEntry(replica);
        if(remove != null) return entries.remove(remove); // overloading equals might allow me to just use remove()
        return false;
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
                        String resource = name.split("::")[0]; // used to give entry a unique name
                        JsonObject contents = entry.getValue().getAsJsonObject();
                        BlockEntry blockEntry = new BlockEntry(resource);
                        blockEntry.read(contents);
                        entries.add(blockEntry);
                    } catch (Exception e) {
                        ;
                    }
                });
            } else {
                JsonObject root = new JsonObject();
                Files.write(file.toPath(), gson.toJson(root).getBytes());
            }
        } catch (Exception e) {
            Wrapper.getMod().printStackTrace(e);
        }
    }

    public void write() {
        try {
            final JsonObject root = new JsonObject();
            entries.forEach(entry -> {
                JsonObject content = new JsonObject();
                entry.write(content);
                root.add(entry.isMetadata() ? (entry.getName() + "::" + entry.getMetadataId()) : entry.getName(), content);
            });
            Files.write(file.toPath(), gson.toJson(root).getBytes());
        } catch (Exception e) {
            Wrapper.getMod().printStackTrace(e);
        }
    }

    public void forEach(Consumer<BlockEntry> consumer) {
        entries.forEach(consumer);
    }
}
