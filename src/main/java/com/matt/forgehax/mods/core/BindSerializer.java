package com.matt.forgehax.mods.core;

import com.google.gson.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.command.CommandBuilder;
import com.matt.forgehax.util.json.GsonConstant;
import com.matt.forgehax.util.mod.BaseMod;
import com.matt.forgehax.util.mod.SilentMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static com.matt.forgehax.Wrapper.getFileManager;
import static com.matt.forgehax.Wrapper.getModManager;
import static com.matt.forgehax.Wrapper.printStackTrace;

/**
 * Created on 4/21/2017 by fr1kin
 */
@RegisterMod
public class BindSerializer extends SilentMod implements GsonConstant {
    private static final BindSerializer INSTANCE = new BindSerializer();

    public static BindSerializer getInstance() {
        return INSTANCE;
    }

    private final File bindingsJson = getFileManager().getFileInConfigDirectory("bindings.json");

    public BindSerializer() {
        super("BindSerializer", "Saves and loads key binds for mods");
    }

    public void serialize() {
        Objects.requireNonNull(bindingsJson, "bindings.json file object is null");
        final JsonObject root = new JsonObject();
        getModManager().getMods().forEach(mod -> mod.getKeyBinds().forEach(bind -> {
            root.addProperty(mod.getModName() + ":" + bind.getKeyDescription(), bind.getKeyCode());
        }));

        try {
            Files.write(bindingsJson.toPath(), GSON_PRETTY.toJson(root).getBytes());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
        } finally {
            Wrapper.getLog().info("ForgeHax binds serialized");
        }
    }

    public void deserialize() {
        Objects.requireNonNull(bindingsJson, "bindings.json file object is null");
        if(!bindingsJson.exists()) return;

        JsonElement head = null;
        try {
            JsonParser parser = new JsonParser();
            head = parser.parse(new String(Files.readAllBytes(bindingsJson.toPath())));
        } catch (IOException e) {
            printStackTrace(e);
        }

        final JsonObject root = (head != null && head.isJsonObject()) ? head.getAsJsonObject() : new JsonObject();

        getModManager().getMods().forEach(mod -> mod.getKeyBinds().forEach(bind -> {
            JsonElement element = root.get(mod.getModName() + ":" + bind.getKeyDescription());
            if(element != null) bind.setKeyCode(element.getAsInt());
        }));

        LOGGER.info("ForgeHax binds deserialized");
    }

    @Override
    protected void onLoad() {
        deserialize();
    }

    @Override
    protected void onUnload() {
        serialize();
    }
}
