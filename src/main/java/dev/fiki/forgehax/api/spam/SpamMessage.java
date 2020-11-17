package dev.fiki.forgehax.api.spam;

import dev.fiki.forgehax.api.common.PriorityEnum;

/**
 * Created on 7/21/2017 by fr1kin
 */
public class SpamMessage implements Comparable<SpamMessage> {
  
  private final String message;
  private final String type;
  private final long delay;
  private final String activator;
  private final PriorityEnum priority;
  
  public SpamMessage(
      String message, String type, long delay, String activator, PriorityEnum priority) {
    this.message = message;
    this.type = type.toLowerCase();
    this.delay = delay;
    this.activator = activator;
    this.priority = priority;
  }
  
  public String getMessage() {
    return message;
  }
  
  public String getType() {
    return type;
  }
  
  public long getDelay() {
    return delay;
  }
  
  public String getActivator() {
    return activator;
  }
  
  @Override
  public int compareTo(SpamMessage o) {
    return priority.compareTo(o.priority);
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj == this
        || (obj instanceof SpamMessage
        && activator != null
        && ((SpamMessage) obj).activator != null
        && activator.equalsIgnoreCase(((SpamMessage) obj).activator));
  }
  
  @Override
  public int hashCode() {
    return activator == null ? super.hashCode() : activator.hashCode();
  }
}
