package dev.fiki.forgehax.api.entity;

/**
 * Created on 6/27/2017 by fr1kin
 */
public enum RelationState {
  /**
   * Is a player
   */
  PLAYER,
  
  /**
   * Mob will attack the player
   */
  HOSTILE,
  
  /**
   * Mob does not attack by default, but will under certain circumstances
   */
  NEUTRAL,
  
  /**
   * Mob is friendly and will not harm the player
   */
  FRIENDLY,
  
  /**
   * Unknown
   */
  INVALID,
  ;
  
  public boolean isValid() {
    return ordinal() > 0;
  }
}
