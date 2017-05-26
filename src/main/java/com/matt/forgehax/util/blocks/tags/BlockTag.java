package com.matt.forgehax.util.blocks.tags;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.json.ISerializableJson;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created on 5/23/2017 by fr1kin
 */
public class BlockTag implements ISerializableJson {
    private final String name;

    private boolean enabled = false;

    public BlockTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void serialize(JsonObject head) {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        head.add(getName(), root);
    }

    @Override
    public void deserialize(JsonObject head) {
        if(head.has(getName())) try {
            JsonObject root = head.get(getName()).getAsJsonObject();
            if(root.has("enabled")) enabled = root.get("enabled").getAsBoolean();
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockTag && getName().equalsIgnoreCase(((BlockTag) obj).getName());
    }
}
