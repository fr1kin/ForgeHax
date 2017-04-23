package com.matt.forgehax.util.key;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.mods.BaseMod;
import net.minecraft.client.settings.KeyBinding;

import java.io.*;
import java.nio.file.Files;
import java.util.Set;

/**
 * Created on 4/21/2017 by fr1kin
 *
 * fucking mojang
 */
public class BindSerializer implements Globals {
    private final File bindingsJson;

    public BindSerializer(File base) {
        this.bindingsJson = new File(base, "bindings.json");
    }

    public void serialize() {
        final JsonObject root = new JsonObject();
        MOD.getMods().forEach((name, mod) -> mod.getKeyBinds().forEach(bind -> {
            root.addProperty(name + ":" + bind.getKeyDescription(), bind.getKeyCode());
        }));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.write(bindingsJson.toPath(), gson.toJson(root).getBytes());
        } catch (IOException e) {
            MOD.printStackTrace(e);
        } finally {
            MOD.getLog().info("ForgeHax binds serialized");
        }
    }

    public void deserialize() {
        if(!bindingsJson.exists()) return;

        JsonElement head = null;
        try {
            JsonParser parser = new JsonParser();
            head = parser.parse(new String(Files.readAllBytes(bindingsJson.toPath())));
        } catch (IOException e) {
            MOD.printStackTrace(e);
        }

        final JsonObject root = (head != null && head.isJsonObject()) ? head.getAsJsonObject() : new JsonObject();

        MOD.getMods().forEach((name, mod) -> mod.getKeyBinds().forEach(bind -> {
            JsonElement element = root.get(name + ":" + bind.getKeyDescription());
            if(element != null) bind.setKeyCode(element.getAsInt());
        }));

        MOD.getLog().info("ForgeHax binds deserialized");
    }
}
