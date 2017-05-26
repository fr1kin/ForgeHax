package com.matt.forgehax.util.blocks.properties;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.matt.forgehax.util.blocks.tags.BlockTag;
import com.matt.forgehax.util.blocks.tags.TagRegistry;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created on 5/23/2017 by fr1kin
 */
public class BlockTagProperty implements IBlockProperty {
    private static final String HEADING = "tags";

    private final Collection<BlockTag> tags = Sets.newHashSet();

    public boolean add(BlockTag tag) {
        return tag != null && tags.add(tag);
    }

    public boolean remove(BlockTag tag) {
        return tag != null && tags.remove(tag);
    }

    public boolean contains(BlockTag tag) {
        return tag != null && tags.contains(tag);
    }

    @Override
    public void serialize(JsonObject head) {
        if(!tags.isEmpty()) {
            final JsonArray array = new JsonArray();
            tags.forEach(tag -> array.add(new JsonPrimitive(tag.getName())));
            head.add(HEADING, array);
        }
    }

    @Override
    public void deserialize(JsonObject head) {
        if(head.has(HEADING)) {
            try {
                tags.clear();
                final JsonArray array = head.get(HEADING).getAsJsonArray();
                array.forEach(e -> {
                    //TODO: figure this out
                });
            } catch (Exception e) {
                ;
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(String.format("%s={", HEADING));
        Iterator<BlockTag> it = tags.iterator();
        while(it.hasNext()) {
            BlockTag tag = it.next();
            builder.append(tag.getName());
            if(it.hasNext()) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
