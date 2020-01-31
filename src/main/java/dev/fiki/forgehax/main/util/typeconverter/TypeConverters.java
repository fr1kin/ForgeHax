package dev.fiki.forgehax.main.util.typeconverter;

import dev.fiki.forgehax.main.util.typeconverter.types.BooleanType;
import dev.fiki.forgehax.main.util.typeconverter.types.ByteType;
import dev.fiki.forgehax.main.util.typeconverter.types.CharacterType;
import dev.fiki.forgehax.main.util.typeconverter.types.DoubleType;
import dev.fiki.forgehax.main.util.typeconverter.types.FloatType;
import dev.fiki.forgehax.main.util.typeconverter.types.IntegerType;
import dev.fiki.forgehax.main.util.typeconverter.types.LongType;
import dev.fiki.forgehax.main.util.typeconverter.types.ShortType;
import dev.fiki.forgehax.main.util.typeconverter.types.StringType;

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
