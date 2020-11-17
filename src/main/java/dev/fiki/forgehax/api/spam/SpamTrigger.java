package dev.fiki.forgehax.api.spam;

/**
 * Created on 7/19/2017 by fr1kin
 */
public enum SpamTrigger {
  /**
   * Triggered every X amount of time
   */
  SPAM,
  /**
   * Triggered when a player enters the keyword
   */
  REPLY,
  
  /**
   * Triggered when a player enters the keyword and has an argument present
   */
  REPLY_WITH_INPUT,
  
  /**
   * Triggered when player connects to server
   */
  PLAYER_CONNECT,
  
  /**
   * Triggered when player disconnects from server
   */
  PLAYER_DISCONNECT
}
