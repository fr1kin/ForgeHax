package com.matt.forgehax.util.blocks.properties;

import com.matt.forgehax.util.serialization.ISerializableJson;

/**
 * Created on 5/20/2017 by fr1kin
 */
public interface IBlockProperty extends ISerializableJson {
  
  /**
   * If the current mutable instance is containing unique data. If this method returns false, then
   * the property may be switched to an immutable instance of itself.
   *
   * @return true if the mutable property has unique data
   */
  boolean isNecessary();

  /**
   * Help text showing what data is currently stored in the property
   *
   * @return formatted data
   */
  String helpText();

  /**
   * Creates a new immutable instance of the class that doesn't allow any data to be mutated from
   * its default state.
   *
   * @return new immutable class
   */
  IBlockProperty newImmutableInstance();

  default <T extends IBlockProperty> T cast() {
    return (T) this;
  }

  default <T extends IBlockProperty> T checkedCast() {
    try {
      return cast();
    } catch (Throwable t) {
      return null;
    }
  }
}
