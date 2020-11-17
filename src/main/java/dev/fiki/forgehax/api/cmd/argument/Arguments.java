package dev.fiki.forgehax.api.cmd.argument;

import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.typeconverter.TypeConverters;
import dev.fiki.forgehax.api.typeconverter.type.EnumType;
import net.minecraft.block.Block;
import net.minecraft.potion.Effect;

public interface Arguments {
  static <T> RawArgument.RawArgumentBuilder<T> newArgument() {
    return RawArgument.<T>builder();
  }

  static <T> RawArgument.RawArgumentBuilder<T> newArgument(Class<T> ctxClass) {
    return RawArgument.<T>builder().type(ctxClass);
  }

  static <T> ConverterArgument.ConverterArgumentBuilder<T> newConverterArgument() {
    return ConverterArgument.builder();
  }

  static <T> ConverterArgument.ConverterArgumentBuilder<T> newConverterArgument(Class<T> clazz) {
    return ConverterArgument.builder();
  }

  static ConverterArgument.ConverterArgumentBuilder<Boolean> newBooleanArgument() {
    return ConverterArgument.<Boolean>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.BOOLEAN);
  }

  static ConverterArgument.ConverterArgumentBuilder<Byte> newByteArgument() {
    return ConverterArgument.<Byte>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.BYTE);
  }

  static ConverterArgument.ConverterArgumentBuilder<Character> newCharacterArgument() {
    return ConverterArgument.<Character>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.CHARACTER);
  }

  static ConverterArgument.ConverterArgumentBuilder<Double> newDoubleArgument() {
    return ConverterArgument.<Double>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.DOUBLE);
  }

  static ConverterArgument.ConverterArgumentBuilder<Float> newFloatArgument() {
    return ConverterArgument.<Float>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.FLOAT);
  }

  static ConverterArgument.ConverterArgumentBuilder<Integer> newIntegerArgument() {
    return ConverterArgument.<Integer>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.INTEGER);
  }

  static ConverterArgument.ConverterArgumentBuilder<Long> newLongArgument() {
    return ConverterArgument.<Long>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.LONG);
  }

  static ConverterArgument.ConverterArgumentBuilder<Short> newShortArgument() {
    return ConverterArgument.<Short>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.SHORT);
  }

  static ConverterArgument.ConverterArgumentBuilder<String> newStringArgument() {
    return ConverterArgument.<String>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(Integer.MAX_VALUE)
        .converter(TypeConverters.STRING);
  }

  static ConverterArgument.ConverterArgumentBuilder<Color> newColorArgument() {
    return ConverterArgument.<Color>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(4)
        .converter(TypeConverters.COLOR);
  }

  static <T extends Enum<T>> ConverterArgument.ConverterArgumentBuilder<T> newEnumArgument(Class<T> contextClass) {
    return ConverterArgument.<T>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(new EnumType<>(contextClass));
  }

  static ConverterArgument.ConverterArgumentBuilder<Block> newBlockArgument() {
    return ConverterArgument.<Block>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.BLOCK);
  }

  static ConverterArgument.ConverterArgumentBuilder<Effect> newEffectArgument() {
    return ConverterArgument.<Effect>builder()
        .minArgumentsConsumed(1)
        .maxArgumentsConsumed(1)
        .converter(TypeConverters.EFFECT);
  }
}
