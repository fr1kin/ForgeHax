package dev.fiki.forgehax.api.entry;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/21/2017 by fr1kin
 */
// TODO: 1.15
public class CustomMessageEntry implements IJsonSerializable {

  @Getter
  @Setter
  private UUID player;
  
  private final List<MessageEntry> messages = Lists.newCopyOnWriteArrayList();
  
  public CustomMessageEntry() {
  }
  
  public List<MessageEntry> getMessages() {
    return Collections.unmodifiableList(messages);
  }
  
  public MessageEntry getEntry(UUID owner) {
    for (MessageEntry entry : messages) {
      if (entry.getOwner().equals(owner)) {
        return entry;
      }
    }
    return null;
  }
  
  public boolean containsEntry(UUID owner) {
    return getEntry(owner) != null;
  }
  
  public void addMessage(UUID owner, String message) {
    MessageEntry entry = getEntry(owner);
    if (entry == null) {
      entry = new MessageEntry();
      messages.add(entry);
    }
    entry.owner = owner;
    entry.setMessage(message);
  }
  
  protected MessageEntry getRandom() {
    return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
  }
  
  public String getRandomMessage() {
    return getRandom().getMessage();
  }
  
  public int getSize() {
    return messages.size();
  }
  
  public void setSize(int size) {
    while (messages.size() > size) {
      messages.remove(getRandom());
    }
  }

  @Override
  public JsonElement serialize() {
    JsonObject head = new JsonObject();

    head.addProperty("uuid", getPlayer().toString());

    JsonArray array = new JsonArray();

    for(MessageEntry entry : messages) {
      JsonObject object = new JsonObject();
      object.addProperty("owner", entry.getOwner().toString());
      object.addProperty("message", entry.getMessage());
      array.add(object);
    }

    head.add("messages", array);

    return head;
  }

  @Override
  public void deserialize(JsonElement json) {
    JsonObject head = json.getAsJsonObject();

    player = UUID.fromString(head.get("uuid").getAsString());

    for(JsonElement e : head.getAsJsonArray("messages")) {
      MessageEntry me = new MessageEntry();
      me.deserialize(e);
      this.messages.add(me);
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this
        || (obj instanceof CustomMessageEntry
        && player.equals(((CustomMessageEntry) obj).getPlayer()))
        || (obj instanceof UUID && player.equals(obj));
  }
  
  @Override
  public int hashCode() {
    return player.hashCode();
  }
  
  @Override
  public String toString() {
    return player.toString();
  }
  
  public static class MessageEntry implements IJsonSerializable {
    
    private UUID owner;
    private String message;
    
    public MessageEntry() { }
    
    public UUID getOwner() {
      return owner;
    }
    
    public String getMessage() {
      return message;
    }
    
    public void setMessage(String message) {
      this.message = message;
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("owner", owner.toString());
      object.addProperty("message", message);
      return object;
    }

    @Override
    public void deserialize(JsonElement json) {
      JsonObject object = json.getAsJsonObject();

      this.owner = UUID.fromString(object.get("owner").getAsString());
      this.message = object.get("message").getAsString();
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof MessageEntry && owner.equals(((MessageEntry) obj).owner))
          || (obj instanceof UUID && owner.equals(obj));
    }
    
    @Override
    public int hashCode() {
      return owner.hashCode();
    }
    
    @Override
    public String toString() {
      return owner.toString();
    }
  }
}
