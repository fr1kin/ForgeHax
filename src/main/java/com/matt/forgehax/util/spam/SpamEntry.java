package com.matt.forgehax.util.spam;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import joptsimple.internal.Strings;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class SpamEntry implements ISerializableJson {
  
  /**
   * A unique name used to identify this entry
   */
  private final String name;
  
  /**
   * List of messages (no duplicates allowed)
   */
  private final List<String> messages = Lists.newCopyOnWriteArrayList();
  
  private boolean enabled = true;
  
  /**
   * Keyword that triggers this
   */
  private String keyword = Strings.EMPTY;
  
  /**
   * How the message should be selected from the list
   */
  private SpamType type = SpamType.RANDOM;
  
  /**
   * What should trigger a message from being outputted
   */
  private SpamTrigger trigger = SpamTrigger.SPAM;
  
  /**
   * Custom delay
   */
  private long delay = 0;
  
  public SpamEntry(String name) {
    this.name = name;
  }
  
  public void add(String msg) {
    if (!Strings.isNullOrEmpty(msg) && !messages.contains(msg)) {
      messages.add(msg);
    }
  }
  
  public void remove(String msg) {
    messages.remove(msg);
  }
  
  private int nextIndex = 0;
  
  public String next() {
    if (!messages.isEmpty()) {
      switch (type) {
        case RANDOM:
          return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        case SEQUENTIAL:
          return messages.get((nextIndex++ % messages.size()));
      }
    }
    return Strings.EMPTY;
  }
  
  public void reset() {
    nextIndex = 0;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public String getName() {
    return name;
  }
  
  public String getKeyword() {
    return keyword;
  }
  
  public SpamType getType() {
    return type;
  }
  
  public SpamTrigger getTrigger() {
    return trigger;
  }
  
  public List<String> getMessages() {
    return Collections.unmodifiableList(messages);
  }
  
  public long getDelay() {
    return delay;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }
  
  public void setType(SpamType type) {
    if (type != null) {
      this.type = type;
    }
  }
  
  public void setType(String type) {
    setType(SpamType.valueOf(type.toUpperCase()));
  }
  
  public void setTrigger(SpamTrigger trigger) {
    if (trigger != null) {
      this.trigger = trigger;
    }
  }
  
  public void setTrigger(String trigger) {
    setTrigger(SpamTrigger.valueOf(trigger.toUpperCase()));
  }
  
  public void setDelay(long delay) {
    this.delay = delay;
  }
  
  public boolean isEmpty() {
    return messages.isEmpty();
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    
    writer.name("enabled");
    writer.value(enabled);
    
    writer.name("keyword");
    writer.value(keyword);
    
    writer.name("type");
    writer.value(type.name());
    
    writer.name("trigger");
    writer.value(trigger.name());
    
    writer.name("delay");
    writer.value(getDelay());
    
    writer.name("messages");
    writer.beginArray();
    for (String msg : messages) {
      writer.value(msg);
    }
    writer.endArray();
    
    writer.endObject();
  }
  
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "enabled":
          setEnabled(reader.nextBoolean());
          break;
        case "keyword":
          setKeyword(reader.nextString());
          break;
        case "type":
          setType(reader.nextString());
          break;
        case "trigger":
          setTrigger(reader.nextString());
          break;
        case "delay":
          setDelay(reader.nextLong());
          break;
        case "messages":
          reader.beginArray();
          while (reader.hasNext()) {
            add(reader.nextString());
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
    return (obj instanceof SpamEntry
      && String.CASE_INSENSITIVE_ORDER.compare(name, ((SpamEntry) obj).name) == 0)
      || (obj instanceof String
      && String.CASE_INSENSITIVE_ORDER.compare(name, (String) obj) == 0);
  }
  
  @Override
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }
  
  @Override
  public String toString() {
    return name;
  }
}
