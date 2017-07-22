package com.matt.forgehax.util.entity;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.serialization.GsonConstant;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerIdHelper implements Globals {
    private static final Map<String, UUID> UUID_CACHE = Maps.newConcurrentMap();

    @Nullable
    public static UUID getIdFast(String name) {
        return UUID_CACHE.getOrDefault(name.toLowerCase(), null);
    }

    public static UUID[] getIds(String... names) {
        final UUID[] uuids = new UUID[names.length];
        // http://wiki.vg/Mojang_API#Playernames_-.3E_UUIDs

        // make sure the player actually exists
        HttpsURLConnection connection = null;
        try {
            // list of player names
            JsonArray players = new JsonArray();
            for(String name : names) players.add(new JsonPrimitive(name));

            URL url = new URL("https://api.mojang.com/profiles/minecraft");

            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(GsonConstant.GSON.toJson(players));
            output.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String json = response.toString();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(json);

            JsonArray array = element.getAsJsonArray();

            if(array.size() != names.length) throw new RuntimeException("Missing names");

            int index = 0;
            for(JsonElement n : array) {
                JsonObject node = n.getAsJsonObject();
                UUID uuid = UUID.fromString(node.get("id").getAsString().replaceFirst (
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                ));
                UUID_CACHE.put(names[index].toLowerCase(), uuid);
                uuids[index] = uuid;
                index++;
            }
        } catch (Throwable t) {
            LOGGER.warn("Could not find all player uuids: " + t.getMessage());
        } finally {
            if(connection != null) connection.disconnect();
        }
        return uuids;
    }

    public static UUID getId(String name) {
        return getIds(name)[0];
    }

    public static UUID getIdEfficiently(String name) {
        UUID id = getIdFast(name);
        if(id == null)
            return getId(name);
        else
            return id;
    }
}
