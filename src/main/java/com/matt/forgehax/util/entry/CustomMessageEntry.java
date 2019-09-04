package com.matt.forgehax.util.entry;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/21/2017 by fr1kin
 */
public class CustomMessageEntry implements ISerializableJson {

  private final UUID player;

  private final List<MessageEntry> messages = Lists.newCopyOnWriteArrayList();

  public CustomMessageEntry(UUID name) {
    this.player = name;
  }

  public CustomMessageEntry(String uuid) {
    this(UUID.fromString(uuid));
  }

  /**
   * The player this join message is for
   */
  public UUID getPlayer() {
    return player;
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
      entry = new MessageEntry(owner);
      messages.add(entry);
    }
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
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();

    writer.name("messages");
    writer.beginArray();
    for (MessageEntry entry : messages) {
      writer.beginObject();
      writer.name(entry.toString());
      entry.serialize(writer);
      writer.endObject();
    }
    writer.endArray();

    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "messages":
          reader.beginArray();
          while (reader.hasNext()) {
            reader.beginObject();
            MessageEntry entry = new MessageEntry(UUID.fromString(reader.nextName()));
            entry.deserialize(reader);
            messages.add(entry);
            reader.endObject();
          }
          reader.endArray();
          break;
        default:
          break;
      }
    }

    reader.endObject();
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

  public static class MessageEntry implements ISerializableJson {
  
    private final UUID owner;
    private String message;

    public MessageEntry(UUID owner) {
      this.owner = owner;
    }

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
    public void serialize(JsonWriter writer) throws IOException {
      writer.beginObject();

      writer.name("msg");
      writer.value(message);

      writer.endObject();
    }

    @Override
    public void deserialize(JsonReader reader) throws IOException {
      reader.beginObject();

      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "msg":
            setMessage(reader.nextString());
            break;
        }
      }

      reader.endObject();
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
