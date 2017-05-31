package com.matt.forgehax.util.container.lists;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.matt.forgehax.Wrapper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.ContainerList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class PlayerList extends ContainerList {
    public PlayerList(String name, File file) {
        super(name, file);
        // initial read
        read();
    }

    public boolean isResponseError(JsonElement json) throws Exception {
        if(json.isJsonObject()) {
            JsonObject root = json.getAsJsonObject();
            if(root.has("error")) {
                String errorMsg = root.get("error").getAsString();
                String reason = "";
                if(root.has("errorMessage"))
                    reason = ": " + root.get("errorMessage").getAsString();
                throw new Exception(errorMsg + reason);
            }
        }
        return false;
    }

    /**
     * Adds player to data file
     * @param name players current name
     * @param uuid players UUID
     * @param nickName custom nickname
     * @return true if added to the list
     */
    public boolean addPlayer(String name, String uuid, String nickName) throws Exception {
        if(name.isEmpty())
            throw new Exception("Empty player name");
        if(uuid.isEmpty())
            throw new Exception("Empty UUID");
        if(contains(uuid))
            throw new Exception("Already contains player in list");
        // if it passes all the exceptions it will add the player
        PlayerData data = new PlayerData(this, new JsonObject())
                .setName(name)
                .setNickName(nickName)
                .setUuid(uuid);
        try {
            requestPlayerProfileData(data);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        data.save();
        return true;
    }

    /**
     * Add player by UUID, will request player names through mojangs server
     * @param uuid uuid of the player
     * @return true if added to the list
     */
    public boolean addPlayerByUUID(UUID uuid) throws Exception {
        String name = "";
        HttpURLConnection connection = null;
        try {
            // other method has a restricts me to 1 request per minute, this will have to do
            URL url = new URL(String.format("https://api.mojang.com/user/profiles/%s/names", uuid.toString().replace("-", "").toLowerCase()));
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);

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
            JsonElement jsonElement = parser.parse(json);
            if(!isResponseError(jsonElement)) {
                JsonArray array = jsonElement.getAsJsonArray();
                // get last entry (should be their current name)
                int last = array.size() - 1;
                if (last > -1) {
                    name = array.get(last).getAsJsonObject().get("name").getAsString();
                } else {
                    throw new Exception("Empty response");
                }
            }
        } catch (MalformedURLException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return addPlayer(name, uuid.toString(), "");
    }

    /**
     * Add player by their name
     * Will send request for players uuid
     * @param name name of the player
     * @return true if added to the list
     */
    public boolean addPlayerByName(String name) throws Exception {
        String uuid = "";
        HttpURLConnection connection = null;
        try {
            Gson gson = new Gson();
            JsonArray postRequest = new JsonArray();
            postRequest.add(new JsonPrimitive(name));
            URL url = new URL("https://api.mojang.com/profiles/minecraft");
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(gson.toJson(postRequest));
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
            JsonElement jsonElement = parser.parse(json);
            if(!isResponseError(jsonElement)) {
                JsonArray array = jsonElement.getAsJsonArray();
                // make sure response is not empty
                if (array.size() > 0) {
                    // get first in array (should be the only one)
                    JsonObject data = array.get(0).getAsJsonObject();
                    if (data.has("id"))
                        uuid = Utils.stringToUUID(data.get("id").getAsString()).toString();
                } else {
                    throw new Exception("Empty response");
                }
            }
        } catch (MalformedURLException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return addPlayer(name, uuid, "");
    }

    /**
     * Add player by entity instance
     * @param player player entity instance
     * @param nickName optional nickname
     * @return true if added to the list
     */
    public boolean addPlayerByEntity(EntityPlayer player, String nickName) throws Exception {
        return addPlayer(player.getName(), player.getUniqueID().toString(), nickName);
    }
    public boolean addPlayerByEntity(EntityPlayer player) throws Exception {
        return addPlayer(player.getName(), player.getUniqueID().toString(), "");
    }

    /**
     * Will get profile data for the player
     * Limited to 1 request a user per minute
     * @param data
     * @return
     * @throws Exception
     */
    public boolean requestPlayerProfileData(PlayerData data) throws Exception {
        if(!data.getSkinURL().isEmpty())
            return false;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(String.format(" https://sessionserver.mojang.com/session/minecraft/profile/%s", data.getUuid().toString().replace("-", "").toLowerCase()));
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);

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
            JsonElement jsonElement = parser.parse(json);
            if(!isResponseError(jsonElement)) {
                JsonObject root = jsonElement.getAsJsonObject();
                if(root.has("name"))
                    data.setName(root.get("name").getAsString());
                if(root.has("properties") &&
                        root.get("properties").isJsonArray()) {
                    JsonArray properties = root.get("properties").getAsJsonArray();
                    for(JsonElement value : properties) {
                        if(value.isJsonObject()) {
                            JsonObject skinRoot = value.getAsJsonObject();
                            if(skinRoot.has("name") &&
                                    skinRoot.has("value") &&
                                    skinRoot.get("name").getAsString().equals("textures")) {
                                String valueEncoded = skinRoot.get("value").getAsString();
                                String decoded = new String(Base64.getDecoder().decode(valueEncoded), "ASCII"); // TODO: maybe change to UTF-8?
                                JsonElement decodedJson = parser.parse(decoded);
                                if(decodedJson.isJsonObject()) {
                                    JsonObject textureProfileJson = decodedJson.getAsJsonObject();
                                    if(textureProfileJson.has("textures")) {
                                        JsonElement texturesJson = textureProfileJson.get("textures");
                                        if(texturesJson.isJsonObject()) {
                                            JsonObject textureDataJson = texturesJson.getAsJsonObject();
                                            // for player skin
                                            if(textureDataJson.has("SKIN")) {
                                                JsonElement skinElement = textureDataJson.get("SKIN");
                                                if(skinElement.isJsonObject()) {
                                                    JsonObject jsonObject = skinElement.getAsJsonObject();
                                                    if(jsonObject.has("url")) {
                                                        data.setSkinURL(jsonObject.get("url").getAsString());
                                                    }
                                                }
                                            }
                                            // for player cape
                                            if(textureDataJson.has("CAPE")) {
                                                JsonElement skinElement = textureDataJson.get("CAPE");
                                                if(skinElement.isJsonObject()) {
                                                    JsonObject jsonObject = skinElement.getAsJsonObject();
                                                    if(jsonObject.has("url")) {
                                                        data.setCapeURL(jsonObject.get("url").getAsString());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } catch (IOException e) {
            Wrapper.printStackTrace(e);
            throw new Exception(e.getMessage());
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return true;
    }

    /**
     * Remove player from the list by UUID
     * @param uuid UUID of the player to remove
     * @return true if removed
     */
    public boolean removePlayerByUUID(String uuid) {
        if(contains(uuid)) {
            boolean ret = remove(uuid);
            save();
            return ret;
        } else return false;
    }
    public boolean removePlayerByUUID(UUID uuid) {
        return removePlayerByUUID(uuid.toString());
    }

    /**
     * Remove player the list by entity instance
     * @param player player instance to remove
     * @return true if removed from list
     */
    public boolean removePlayerByEntity(EntityPlayer player) {
        return removePlayerByUUID(player.getUniqueID().toString());
    }

    /**
     * Check if the list contains a player
     */
    public boolean containsPlayer(String uuid) {
        return contains(uuid);
    }
    public boolean containsPlayer(EntityPlayer player) {
        return contains(player.getUniqueID().toString());
    }

    public PlayerData getPlayerData(String uuid) {
        if(contains(uuid)) {
            JsonElement root = get(uuid);
            return root.isJsonObject() ? (new PlayerData(this, root.getAsJsonObject()).setUuid(uuid)) : null;
        }
        else return null;
    }

    public void savePlayerData(PlayerData data) {
        add(data.getUuid().toString(), data.getRoot());
    }

    public static class PlayerData {
        private final PlayerList parent;
        private final JsonObject root;
        private UUID uuid;

        public PlayerData(PlayerList parent, JsonObject root) {
            this.parent = parent;
            this.root = root;
        }
        public PlayerData(JsonObject root) {
            this.parent = null;
            this.root = root;
        }

        /**
         * Get root json object
         */
        public JsonObject getRoot() {
            return root;
        }

        /**
         * Set player UUID
         */
        public PlayerData setUuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        public PlayerData setUuid(String uuid) {
            this.uuid = Utils.stringToUUID(uuid);
            return this;
        }

        /**
         * Get player UUID
         */
        public UUID getUuid() {
            return uuid;
        }

        /**
         * Set player name
         */
        public PlayerData setName(String name) {
            if(!name.isEmpty())
                root.addProperty("name", name);
            return this;
        }

        /**
         * Get player name
         */
        public String getName() {
            JsonElement element = root.get("name");
            return element != null ? element.getAsString() : "";
        }

        /**
         * Set players nickname
         */
        public PlayerData setNickName(String nickName) {
            if(!nickName.isEmpty())
                root.addProperty("nick", nickName);
            return this;
        }

        /**
         * Get players nick name (if none exists, get name)
         */
        public String getNickName() {
            JsonElement element = root.get("nick");
            return element != null ? element.getAsString() : "";
        }

        /**
         * Get display name for GUIs
         */
        public String getGuiName() {
            String nick = getNickName();
            return !nick.isEmpty() ? String.format("%s (%s)", getName(), nick) : getName();
        }

        /**
         * Name to be displayed
         */
        public String getDisplayName() {
            String nick = getNickName();
            return nick.isEmpty() ? getName() : nick;
        }


        public PlayerData setSkinURL(String skinURL) {
            root.addProperty("skin_url", skinURL);
            return this;
        }

        public String getSkinURL() {
            return root.has("skin_url") ? root.get("skin_url").getAsString() : "";
        }

        public PlayerData setCapeURL(String capeURL) {
            root.addProperty("cape_url", capeURL);
            return this;
        }

        public String getCapeURL() {
            return root.has("cape_url") ? root.get("cape_url").getAsString() : "";
        }

        public boolean save() {
            if(parent != null) {
                parent.savePlayerData(this);
                parent.save();
            }
            return false;
        }
    }
}
