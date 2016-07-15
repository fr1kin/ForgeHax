package com.matt.forgehax.util.container;

import com.google.gson.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class PlayerList extends ContainerList {
    public PlayerList(String name, File file) {
        super(name, file);
        // initial read
        read();
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        File rootDir = new File(projectPath, "run");
        File dir = new File(rootDir, "players.json");

        System.out.printf("path: %s\n", dir);
        PlayerList list = new PlayerList("players", dir);
        //21810c8f-d0e5-4cbd-bd9a-c22826b9d97a
        list.addPlayer("fr1kin");
        list.addPlayer(UUID.fromString("8f2ce453-cef2-4b3e-b686-dc21b519a0a1"));
    }

    public boolean addPlayer(String name, String uuid, String nickName) {
        if(!contains(uuid)) {
            add(uuid, new PlayerData(name, nickName).toJson());
            save();
            return true;
        } else return false;
    }

    // TODO: thread requests
    public boolean addPlayer(UUID strUuid) {
        String name = "<unknown>";
        String uuid = strUuid.toString();
        HttpURLConnection connection = null;
        try {
            // other method has a restricts me to 1 request per minute, this will have to do
            JsonArray postRequest = new JsonArray();
            postRequest.add(new JsonPrimitive(uuid));
            URL url = new URL(String.format("https://api.mojang.com/user/profiles/%s/names", uuid.replace("-", "").toLowerCase()));
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
            String line;
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String json = response.toString();
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(json).getAsJsonArray();
            // get last entry (should be their current name)
            int last = array.size() - 1;
            if(last > -1) {
                name = array.get(last).getAsJsonObject().get("name").getAsString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return addPlayer(name, uuid, "");
    }
    public boolean addPlayer(String name) {
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
            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
            String line;
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            String json = response.toString();
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(json).getAsJsonArray();
            if(array.size() > 0) {
                JsonObject data = array.get(0).getAsJsonObject();
                if(data.has("id"))
                    uuid = UUID.fromString(data.get("id").getAsString().replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")).toString(); // verify
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return addPlayer(name, uuid, "");
    }
    public boolean addPlayer(EntityPlayer player, String nickName) {
        return addPlayer(player.getName(), player.getUniqueID().toString(), nickName);
    }
    public boolean addPlayer(EntityPlayer player) {
        return addPlayer(player.getName(), player.getUniqueID().toString(), "");
    }

    public boolean removePlayer(String uuid) {
        if(contains(uuid)) {
            remove(uuid);
            save();
            return true;
        } else return false;
    }
    public boolean removePlayer(EntityPlayer player) {
        return removePlayer(player.getUniqueID().toString());
    }

    public boolean containsPlayer(String uuid) {
        return contains(uuid);
    }
    public boolean containsPlayer(EntityPlayer player) {
        return contains(player.getUniqueID().toString());
    }

    public PlayerData getPlayerData(String uuid) {
        if(contains(uuid))
            return PlayerData.parseJson(get(uuid).getAsJsonObject());
        else return null;
    }

    public String getPlayerName(String uuid) {
        if(contains(uuid))
            return PlayerData.getNameFromJson(get(uuid).getAsJsonObject());
        else return "";
    }

    public String getPlayerNickName(String uuid) {
        if(contains(uuid))
            return PlayerData.getNickNameFromJson(get(uuid).getAsJsonObject());
        else return "";
    }

    public static class PlayerData {
        private String name = "";
        private String nickName = "";

        public PlayerData(String name, String nickName) {
            this.name = name;
            this.nickName = nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getName() {
            return name;
        }

        public String getNickName() {
            return !nickName.isEmpty() ? nickName : name;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            if(!nickName.isEmpty())
                json.addProperty("nick", nickName);
            return json;
        }

        public static PlayerData parseJson(JsonObject json) {
            String name = "", nick = "";
            if(json.has("name"))
                name = json.get("name").getAsString();
            if(json.has("nick"))
                nick = json.get("nick").getAsString();
            return new PlayerData(name, nick);
        }

        public static String getNameFromJson(JsonObject json) {
            if(json.has("name"))
                return json.get("name").getAsString();
            return "";
        }

        public static String getNickNameFromJson(JsonObject json) {
            if(json.has("nick"))
                return json.get("nick").getAsString();
            return "";
        }
    }
}
