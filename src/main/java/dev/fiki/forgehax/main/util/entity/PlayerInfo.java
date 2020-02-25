package dev.fiki.forgehax.main.util.entity;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.serialization.GsonConstant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.entity.player.PlayerEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;

/**
 * Created on 7/22/2017 by fr1kin
 */
@Getter
public class PlayerInfo implements Common, GsonConstant {

  final List<Name> names = Lists.newCopyOnWriteArrayList();

  private final String username;
  private final UUID uuid;

  private final boolean offlinePlayer;

  private boolean connected = false;

//  public PlayerInfo(UUID id) throws IOException {
//    Objects.requireNonNull(id);
//    this.uuid = id;
//    this.names = ImmutableList.copyOf(getNameHistory(id));
//    this.offlineId = PlayerEntity.getOfflineUUID(getName());
//    this.isOfflinePlayer = false;
//  }
//
//  public PlayerInfo(String name) throws IOException, NullPointerException {
//    Objects.requireNonNull(name);
//    JsonArray ar = new JsonArray();
//    ar.add(name);
//
//    JsonArray array =
//        getResources(new URL("https://api.mojang.com/profiles/minecraft"), "POST", ar)
//            .getAsJsonArray();
//    JsonObject node = array.get(0).getAsJsonObject();
//
//    UUID uuid = PlayerInfoHelper.getIdFromString(node.get("id").getAsString());
//    Objects.requireNonNull(uuid);
//
//    this.uuid = uuid;
//    this.names = ImmutableList.copyOf(getNameHistory(uuid));
//    this.offlineId = PlayerEntity.getOfflineUUID(name);
//    this.isOfflinePlayer = false;
//  }
//
//  public PlayerInfo(String name, boolean dummy) {
//    this.uuid = PlayerEntity.getOfflineUUID(name);
//    this.names = Collections.singletonList(new Name(name));
//    this.offlineId = this.uuid;
//    this.isOfflinePlayer = true;
//  }

  public PlayerInfo(String username, UUID uuid, boolean offlinePlayer) {
    this.username = username;
    this.uuid = uuid;
    this.offlinePlayer = offlinePlayer;
  }

  void setNames(Collection<Name> names) {
    this.names.clear();
    this.names.addAll(names);
  }

  public UUID getOfflineId() {
    return PlayerEntity.getOfflineUUID(username);
  }

  /**
   * If this player is not verified on Mojang's auth server
   */
  public boolean isOfflinePlayer() {
    return uuid == null;
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
    return names.stream()
        .map(Name::getName)
        .collect(Collectors.joining(", "));
  }

  public boolean isLocalPlayer() {
    return String.CASE_INSENSITIVE_ORDER.compare(getName(), getLocalPlayer().getGameProfile().getName()) == 0;
  }

  public boolean matches(UUID otherId) {
    return otherId != null && (otherId.equals(getOfflineId()) || otherId.equals(getUuid()));
  }

  public GameProfile toGameProfile() {
    return new GameProfile(getUuid(), getUsername());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PlayerInfo && uuid.equals(((PlayerInfo) obj).uuid);
  }

  @Override
  public int hashCode() {
    return uuid.hashCode();
  }

  @Override
  public String toString() {
    return uuid.toString();
  }

  @SneakyThrows
  static UUID getUuidFromName(String username) {
    JsonArray request = new JsonArray();
    request.add(username);

    JsonArray response = getResources(
        new URL("https://api.mojang.com/profiles/minecraft"),
        "POST", request
    ).getAsJsonArray();

    JsonObject node = response.get(0).getAsJsonObject();
    return PlayerInfoHelper.getIdFromString(node.get("id").getAsString());
  }

  @SneakyThrows
  static List<Name> getNameHistory(UUID id) {
    JsonArray array = getResources(
        new URL("https://api.mojang.com/user/profiles/"
            + PlayerInfoHelper.getIdNoHyphens(id)
            + "/names"),
        "GET"
    ).getAsJsonArray();
    return StreamSupport.stream(array.spliterator(), false)
        .map(JsonElement::getAsJsonObject)
        .map(node -> new Name(
            node.get("name").getAsString(),
            node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0))
        .sorted()
        .collect(Collectors.toList());
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

  @Getter
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class Name implements Comparable<Name> {
    private final String name;
    private final long changedAt;

    public Name(String name) {
      this(name, 0);
    }

    @Override
    public int compareTo(Name o) {
      return Long.compare(o.changedAt, changedAt); // longest to shortest
    }
  }
}
