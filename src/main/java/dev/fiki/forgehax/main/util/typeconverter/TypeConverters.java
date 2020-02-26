package dev.fiki.forgehax.main.util.typeconverter;

import dev.fiki.forgehax.main.util.color.Color;
import dev.fiki.forgehax.main.util.key.KeyInput;
import dev.fiki.forgehax.main.util.typeconverter.primitive.*;
import dev.fiki.forgehax.main.util.typeconverter.type.ColorType;
import dev.fiki.forgehax.main.util.typeconverter.type.InputType;
import dev.fiki.forgehax.main.util.typeconverter.type.KeyInputType;
import dev.fiki.forgehax.main.util.typeconverter.type.PatternType;
import net.minecraft.client.util.InputMappings;

import java.util.regex.Pattern;

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
  TypeConverter<String> STRING_CASE_INSENSITIVE = new StringType.CaseInsensitive();

  TypeConverter<Color> COLOR = new ColorType();
  TypeConverter<InputMappings.Input> INPUT = new InputType();
  TypeConverter<KeyInput> KEY_INPUT = new KeyInputType();
  TypeConverter<Pattern> PATTERN = new PatternType();
}
