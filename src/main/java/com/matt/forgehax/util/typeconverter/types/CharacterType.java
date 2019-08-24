package com.matt.forgehax.util.typeconverter.types;

import com.google.common.base.Strings;
import com.matt.forgehax.util.typeconverter.TypeConverter;
import java.util.Comparator;
import javax.annotation.Nullable;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class CharacterType extends TypeConverter<Character> {
  
  @Override
  public String label() {
    return "char";
  }

  @Override
  public Class<Character> type() {
    return Character.class;
  }

  @Override
  public Character parse(String value) {
    return Strings.isNullOrEmpty(value) ? '\0' : value.charAt(0);
  }

  @Override
  public String toString(Character value) {
    return Character.toString(value);
  }

  @Nullable
  @Override
  public Comparator<Character> comparator() {
    return Character::compare;
  }
}
