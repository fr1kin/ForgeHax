package com.matt.forgehax.util.typeconverter;

import com.matt.forgehax.util.typeconverter.types.BooleanType;
import com.matt.forgehax.util.typeconverter.types.ByteType;
import com.matt.forgehax.util.typeconverter.types.CharacterType;
import com.matt.forgehax.util.typeconverter.types.DoubleType;
import com.matt.forgehax.util.typeconverter.types.FloatType;
import com.matt.forgehax.util.typeconverter.types.IntegerType;
import com.matt.forgehax.util.typeconverter.types.LongType;
import com.matt.forgehax.util.typeconverter.types.ShortType;
import com.matt.forgehax.util.typeconverter.types.StringType;

/**
 * Created on 3/23/2017 by fr1kin
 */
public interface TypeConverters {
  
  TypeConverter<Boolean> BOOLEAN = new BooleanType();
  TypeConverter<Byte> BYTE = new ByteType();
  TypeConverter<Character> CHARACTER = new CharacterType();
  TypeConverter<Double> DOUBLE = new DoubleType();
  TypeConverter<Float> FLOAT = new FloatType();
  TypeConverter<Integer> INTEGER = new IntegerType();
  TypeConverter<Long> LONG = new LongType();
  TypeConverter<Short> SHORT = new ShortType();
  TypeConverter<String> STRING = new StringType();
}
