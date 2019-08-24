package com.matt.forgehax.util.entity;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matt.forgehax.Globals;
import com.matt.forgehax.util.serialization.GsonConstant;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Created on 7/22/2017 by fr1kin
 */
public class PlayerInfo implements Globals, GsonConstant {
  
  /**
   * The online UUID for this player
   */
  private final UUID id;
  
  private final UUID offlineId;
  
  /**
   * If this player data is only for offline mode
   */
  private final boolean isOfflinePlayer;
  
  /**
   * List of names
   */
  private final List<Name> names;
  
  public PlayerInfo(UUID id) throws IOException {
    Objects.requireNonNull(id);
    this.id = id;
    this.names = ImmutableList.copyOf(lookupNames(id));
    this.offlineId = EntityPlayerSP.getOfflineUUID(getName());
    this.isOfflinePlayer = false;
  }
  
  public PlayerInfo(String name) throws IOException, NullPointerException {
    Objects.requireNonNull(name);
    JsonArray ar = new JsonArray();
    ar.add(name);
    
    JsonArray array =
      getResources(new URL("https://api.mojang.com/profiles/minecraft"), "POST", ar)
        .getAsJsonArray();
    JsonObject node = array.get(0).getAsJsonObject();
    
    UUID uuid = PlayerInfoHelper.getIdFromString(node.get("id").getAsString());
    Objects.requireNonNull(uuid);
    
    this.id = uuid;
    this.names = ImmutableList.copyOf(lookupNames(uuid));
    this.offlineId = EntityPlayerSP.getOfflineUUID(name);
    this.isOfflinePlayer = false;
  }
  
  public PlayerInfo(String name, boolean dummy) {
    this.id = EntityPlayerSP.getOfflineUUID(name);
    this.names = Collections.singletonList(new Name(name));
    this.offlineId = this.id;
    this.isOfflinePlayer = true;
  }
  
  private static List<Name> lookupNames(UUID id) throws IOException {
    JsonArray array =
      getResources(
        new URL(
          "https://api.mojang.com/user/profiles/"
            + PlayerInfoHelper.getIdNoHyphens(id)
            + "/names"),
        "GET")
        .getAsJsonArray();
    List<Name> temp = Lists.newArrayList();
    for (JsonElement e : array) {
      JsonObject node = e.getAsJsonObject();
      String name = node.get("name").getAsString();
      long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0;
      temp.add(new Name(name, changedAt));
    }
    Collections.sort(temp);
    return temp;
  }
  
  /**
   * Unique ID that will identify this player
   */
  public UUID getId() {
    return id;
  }
  
  public UUID getOfflineId() {
    return offlineId;
  }
  
  /**
   * If this player is not verified on Mojang's auth server
   */
  public boolean isOfflinePlayer() {
    return isOfflinePlayer;
  }
  
  /**
   * This players current name
   */
  public String getName() {
    if (!names.isEmpty()) {
      return names.get(0).getName();
    } else {
      return null;
    }
  }
  
  /**
   * This players name history
   */
  public List<Name> getNameHistory() {
    return names;
  }
  
  public String getNameHistoryAsString() {
    StringBuilder builder = new StringBuilder();
    if (!names.isEmpty()) {
      Iterator<Name> it = names.iterator();
      it.next(); // skip first name
      while (it.hasNext()) {
        Name next = it.next();
        builder.append(next.getName());
        if (it.hasNext()) {
          builder.append(", ");
        }
      }
    }
    return builder.toString();
  }
  
  public boolean isLocalPlayer() {
    return String.CASE_INSENSITIVE_ORDER.compare(getName(), getLocalPlayer().getName()) == 0;
  }
  
  public boolean matches(UUID otherId) {
    return otherId != null && (otherId.equals(getOfflineId()) || otherId.equals(getId()));
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
  
  private static JsonElement getResources(URL url, String request, JsonElement element)
    throws IOException {
    JsonElement data;
    HttpsURLConnection connection = null;
    try {
      connection = (HttpsURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod(request);
      connection.setRequestProperty("Content-Type", "application/json");
  
      if (element != null) {
        DataOutputStream output = new DataOutputStream(connection.getOutputStream());
        output.writeBytes(GSON.toJson(element));
        output.close();
      }
  
      Scanner scanner = new Scanner(connection.getInputStream());
      StringBuilder builder = new StringBuilder();
      while (scanner.hasNextLine()) {
        builder.append(scanner.nextLine());
        builder.append('\n');
      }
      scanner.close();
  
      String json = builder.toString();
      data = PARSER.parse(json);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return data;
  }
  
  private static JsonElement getResources(URL url, String request) throws IOException {
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
      this(name, 0);
    }
    
    public String getName() {
      return name;
    }
    
    public long getTimeChanged() {
      return changedAt;
    }
    
    @Override
    public int compareTo(Name o) {
      return Long.compare(o.changedAt, changedAt); // longest to shortest
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj instanceof Name
        && name.equalsIgnoreCase(((Name) obj).getName())
        && changedAt == ((Name) obj).changedAt;
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(name, changedAt);
    }
  }
}
