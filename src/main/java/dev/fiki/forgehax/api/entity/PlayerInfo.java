package dev.fiki.forgehax.api.entity;

import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.main.Common;
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
public class PlayerInfo implements Common {

  final List<Name> names = Lists.newCopyOnWriteArrayList();

  private final String username;
  private final UUID uuid;

  private final boolean offlinePlayer;

  private boolean connected = false;

  public PlayerInfo(String username, UUID uuid) {
    this.username = username;
    this.uuid = uuid;
    this.offlinePlayer = username.isEmpty() || uuid.equals(getOfflineId());
  }

  public PlayerInfo setConnected(boolean connected) {
    this.connected = connected;
    return this;
  }

  void setNames(Collection<Name> names) {
    this.names.clear();
    this.names.addAll(names);
  }

  public UUID getOfflineId() {
    return PlayerEntity.createPlayerUUID(username);
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
        output.writeBytes(new Gson().toJson(element));
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
      data = new JsonParser().parse(json);
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
