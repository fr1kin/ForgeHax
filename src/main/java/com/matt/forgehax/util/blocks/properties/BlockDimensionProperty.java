package com.matt.forgehax.util.blocks.properties;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.matt.forgehax.util.json.ISerializableJson;
import joptsimple.internal.Strings;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created on 5/23/2017 by fr1kin
 */
public class BlockDimensionProperty implements IBlockProperty {
    private static final String HEADING = "dimensions";

    private Collection<String> dimensions = Sets.newHashSet();

    private boolean add(String name) {
        try {
            return !Strings.isNullOrEmpty(name) && dimensions.add(name);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean add(int id) {
        try {
            return add(DimensionManager.getProviderType(id).getName());
        } catch (Exception e) {
            ; // will throw exception if id does not exist
            return false;
        }
    }

    private boolean remove(String name) {
        try {
            return !Strings.isNullOrEmpty(name) && dimensions.remove(name);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean remove(int id) {
        try {
            return remove(DimensionManager.getProviderType(id).getName());
        } catch (Exception e) {
            return false; // will throw exception if id does not exist
        }
    }

    public boolean contains(int id) {
        try {
            return dimensions.contains(DimensionManager.getProviderType(id).getName());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidDimension(int id) {
        return dimensions.isEmpty() || contains(id);
    }

    @Override
    public void serialize(JsonObject head) {
        if(!dimensions.isEmpty()) {
            final JsonArray array = new JsonArray();
            dimensions.forEach(dimension -> array.add(new JsonPrimitive(dimension)));
            head.add(HEADING, array);
        }
    }

    @Override
    public void deserialize(JsonObject head) {
        if(head.has(HEADING)) {
            dimensions.clear();
            JsonArray array = head.get(HEADING).getAsJsonArray();
            array.forEach(dim -> add(dim.getAsString()));
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(String.format("%s={", HEADING));
        Iterator<String> it = dimensions.iterator();
        while(it.hasNext()) {
            String name = it.next();
            builder.append(name);
            if(it.hasNext()) builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }
}
