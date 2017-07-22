package com.matt.forgehax.util.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.serialization.GsonConstant;
import net.minecraft.client.entity.EntityPlayerSP;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.util.*;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfo implements Globals, GsonConstant {
    /**
     * The online UUID for this player
     */
    private final UUID id;

    /**
     * If this player data is only for offline mode
     */
    private final boolean isOfflinePlayer;

    /**
     * List of names
     */
    private final List<Name> names;

    public PlayerInfo(UUID id) {
        this.id = id;
        List<Name> temp;
        try {
            JsonArray array = getResources(new URL("https://api.mojang.com/user/profiles/" + PlayerInfoHelper.getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
            temp = Lists.newArrayList();
            for (JsonElement e : array) {
                JsonObject node = e.getAsJsonObject();
                String name = node.get("name").getAsString();
                long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : -1;
                temp.add(new Name(name, changedAt));
            }
            Collections.sort(temp);
        } catch (Throwable t) {
            temp = Collections.emptyList();
        }
        this.names = ImmutableList.copyOf(temp);
        this.isOfflinePlayer = false;
    }
    public PlayerInfo(String name) {
        JsonArray ar = new JsonArray();
        ar.add(name);

        UUID _id = UUID.randomUUID();
        List<Name> _temp = Collections.emptyList();
        boolean _offline = true;

        try {
            JsonArray array = getResources(new URL("https://api.mojang.com/profiles/minecraft"), "POST", ar).getAsJsonArray();
            JsonObject node = array.get(0).getAsJsonObject();

            UUID uuid = PlayerInfoHelper.getIdFromString(node.get("id").getAsString());
            Objects.requireNonNull(uuid);

            array = getResources(new URL("https://api.mojang.com/user/profiles/" + PlayerInfoHelper.getIdNoHyphens(uuid) + "/names"), "GET").getAsJsonArray();
            List<Name> temp = Lists.newArrayList();
            for(JsonElement e : array) {
                JsonObject n = e.getAsJsonObject();
                String nm = n.get("name").getAsString();
                long changedAt = n.has("changedToAt") ? n.get("changedToAt").getAsLong() : -1;
                temp.add(new Name(nm, changedAt));
            }
            Collections.sort(temp);
            _id = uuid;
            _temp = ImmutableList.copyOf(temp);
            _offline = false;
        } catch (Throwable t) {
            _id = EntityPlayerSP.getOfflineUUID(name);
            _temp = Collections.singletonList(new Name(name));
            _offline = true;
        } finally {
            this.id = _id;
            this.names = _temp;
            this.isOfflinePlayer = _offline;
        }
    }

    /**
     * Unique ID that will identify this player
     * @return
     */
    public UUID getId() {
        return id;
    }

    /**
     * If this player is not verified on Mojang's auth server
     * @return
     */
    public boolean isOfflinePlayer() {
        return isOfflinePlayer;
    }

    /**
     * This players current name
     * @return
     */
    public String getName() {
        if(names.size() > 0)
            return names.get(0).getName();
        else
            return null;
    }

    /**
     * This players name history
     * @return
     */
    public List<Name> getNameHistory() {
        return names;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlayerInfo && id.equals(((PlayerInfo) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private static JsonElement getResources(URL url, String request, JsonElement element) throws Exception {
        JsonElement data;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(request);
            connection.setRequestProperty("Content-Type", "application/json");

            if(element != null) {
                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                output.writeBytes(GSON.toJson(element));
                output.close();
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder builder = new StringBuilder();
            while(scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
                builder.append('\n');
            }
            scanner.close();

            String json = builder.toString();
            data = PARSER.parse(json);
        } finally {
            if(connection != null) connection.disconnect();
        }
        return data;
    }
    private static JsonElement getResources(URL url, String request) throws Exception {
        return getResources(url, request, null);
    }

    public static class Name implements Comparable<Name> {
        private final String name;
        private final long changedAt;

        public Name(String name, long changedAt) {
            this.name = name;
            this.changedAt = changedAt;
        }
        public Name(String name) {
            this(name, -1);
        }

        public String getName() {
            return name;
        }

        public long getChangedAt() {
            return changedAt;
        }

        public boolean isCurrentName() {
            return changedAt == -1;
        }

        @Override
        public int compareTo(Name o) {
            if(changedAt == -1 && o.changedAt == -1)
                return 0; // equal
            else if(changedAt == -1)
                return 1; // greater than
            else if(o.changedAt == -1)
                return -1; // less than
            else
                return Long.compare(changedAt, o.changedAt);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Name && name.equalsIgnoreCase(((Name) obj).getName()) && changedAt == ((Name) obj).changedAt;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, changedAt);
        }
    }
}
