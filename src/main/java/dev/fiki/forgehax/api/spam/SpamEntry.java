package dev.fiki.forgehax.api.spam;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import joptsimple.internal.Strings;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 7/18/2017 by fr1kin
 */
public class SpamEntry implements IJsonSerializable {
  
  /**
   * A unique name used to identify this entry
   */
  private String name;
  
  /**
   * List of messages (no duplicates allowed)
   */
  private final List<String> messages = Lists.newCopyOnWriteArrayList();
  
  private boolean enabled = false;
  
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
  
  public SpamEntry() { }
  
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
  public JsonElement serialize() {
    JsonObject object = new JsonObject();

    object.addProperty("name", getName());
    object.addProperty("enabled", isEnabled());
    object.addProperty("keyword", getKeyword());
    object.addProperty("type", getType().name());
    object.addProperty("trigger", getTrigger().name());

    return object;
  }

  @Override
  public void deserialize(JsonElement json) {
    JsonObject object = json.getAsJsonObject();

    this.name = object.get("name").getAsString();
    this.enabled = object.get("enabled").getAsBoolean();
    this.keyword = object.get("keyword").getAsString();
    this.type = SpamType.valueOf(object.get("type").getAsString());
    this.trigger = SpamTrigger.valueOf(object.get("trigger").getAsString());
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
