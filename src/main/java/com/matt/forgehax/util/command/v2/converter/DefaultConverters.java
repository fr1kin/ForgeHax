package com.matt.forgehax.util.command.v2.converter;

import com.matt.forgehax.util.command.v2.converter.exceptions.StringConversionException;
import com.matt.forgehax.util.command.v2.converter.exceptions.ValueParseException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface DefaultConverters {
  IConverter<Boolean> BOOLEAN =
      new AbstractConverter<Boolean>() {
        private final String ACCEPTABLE_TRUE_BOOLEAN_STRINGS =
            Stream.of(
                    "on", "t", "tr", "tru", "true", "e", "en", "ena", "enab", "enabl", "enable",
                    "enabled", "1")
                .collect(Collectors.joining("|"));

        @Override
        public Class<Boolean> getType() {
          return Boolean.class;
        }

        @Override
        public Class<?> getPrimitiveType() {
          return boolean.class;
        }

        @Override
        public String getLabel() {
          return "boolean";
        }

        @Override
        public Stream<Boolean> valuesOf(String input, Stream<Boolean> stream)
            throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Boolean valueOf(String input, Stream<Boolean> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          return input.toLowerCase().matches(ACCEPTABLE_TRUE_BOOLEAN_STRINGS);
        }

        @Override
        public String toString(Boolean value) throws StringConversionException {
          return Boolean.toString(value);
        }

        @Override
        public int compare(Boolean o1, Boolean o2) {
          return Boolean.compare(o1, o2);
        }
      };

  IConverter<Byte> BYTE =
      new AbstractConverter<Byte>() {
        @Override
        public Class<Byte> getType() {
          return Byte.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return byte.class;
        }

        @Override
        public String getLabel() {
          return "byte";
        }

        @Override
        public Stream<Byte> valuesOf(String input, Stream<Byte> stream) throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Byte valueOf(String input, Stream<Byte> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Byte.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Byte value) throws StringConversionException {
          return Byte.toString(value);
        }

        @Override
        public int compare(Byte o1, Byte o2) {
          return Byte.compare(o1, o2);
        }
      };

  IConverter<Character> CHARACTER =
      new AbstractConverter<Character>() {
        @Override
        public Class<Character> getType() {
          return Character.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return char.class;
        }

        @Override
        public String getLabel() {
          return "char";
        }

        @Override
        public Stream<Character> valuesOf(String input, Stream<Character> stream) {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Character valueOf(String input, Stream<Character> stream)
            throws ValueParseException {
          ValueParseException.requireNonNull(input);
          if (input.length() != 1)
            throw new ValueParseException(
                new IllegalArgumentException(
                    "String \"" + String.valueOf(input) + "\" must be 1 character in length"));
          return input.charAt(0);
        }

        @Override
        public String toString(Character value) throws StringConversionException {
          return String.valueOf(value);
        }

        @Override
        public int compare(Character o1, Character o2) {
          return Character.compare(o1, o2);
        }
      };

  IConverter<Double> DOUBLE =
      new AbstractConverter<Double>() {
        @Override
        public Class<Double> getType() {
          return Double.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return double.class;
        }

        @Override
        public String getLabel() {
          return "double";
        }

        @Override
        public Stream<Double> valuesOf(String input, Stream<Double> stream) {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Double valueOf(String input, Stream<Double> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Double.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Double value) throws StringConversionException {
          return Double.toString(value);
        }

        @Override
        public int compare(Double o1, Double o2) {
          return Double.compare(o1, o2);
        }
      };

  IConverter<Float> FLOAT =
      new AbstractConverter<Float>() {
        @Override
        public Class<Float> getType() {
          return Float.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return float.class;
        }

        @Override
        public String getLabel() {
          return "float";
        }

        @Override
        public Stream<Float> valuesOf(String input, Stream<Float> stream) {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Float valueOf(String input, Stream<Float> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Float.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Float value) throws StringConversionException {
          return Float.toString(value);
        }

        @Override
        public int compare(Float o1, Float o2) {
          return Float.compare(o1, o2);
        }
      };

  IConverter<Integer> INTEGER =
      new AbstractConverter<Integer>() {
        @Override
        public Class<Integer> getType() {
          return Integer.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return int.class;
        }

        @Override
        public String getLabel() {
          return "integer";
        }

        @Override
        public Stream<Integer> valuesOf(String input, Stream<Integer> stream)
            throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Integer valueOf(String input, Stream<Integer> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Integer.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Integer value) throws StringConversionException {
          return Integer.toString(value);
        }

        @Override
        public int compare(Integer o1, Integer o2) {
          return Integer.compare(o1, o2);
        }
      };

  IConverter<Long> LONG =
      new AbstractConverter<Long>() {
        @Override
        public Class<Long> getType() {
          return Long.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return long.class;
        }

        @Override
        public String getLabel() {
          return "long";
        }

        @Override
        public Stream<Long> valuesOf(String input, Stream<Long> stream) throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Long valueOf(String input, Stream<Long> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Long.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Long value) throws StringConversionException {
          return Long.toString(value);
        }

        @Override
        public int compare(Long o1, Long o2) {
          return Long.compare(o1, o2);
        }
      };

  IConverter<Short> SHORT =
      new AbstractConverter<Short>() {
        @Override
        public Class<Short> getType() {
          return Short.class;
        }

        @Nullable
        @Override
        public Class<?> getPrimitiveType() {
          return short.class;
        }

        @Override
        public String getLabel() {
          return "short";
        }

        @Override
        public Stream<Short> valuesOf(String input, Stream<Short> stream)
            throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public Short valueOf(String input, Stream<Short> stream) throws ValueParseException {
          ValueParseException.requireNonNull(input);
          try {
            return Short.valueOf(input);
          } catch (NumberFormatException e) {
            throw new ValueParseException(e);
          }
        }

        @Override
        public String toString(Short value) throws StringConversionException {
          return Short.toString(value);
        }

        @Override
        public int compare(Short o1, Short o2) {
          return Short.compare(o1, o2);
        }
      };

  IConverter<String> STRING =
      new AbstractConverter<String>() {
        @Override
        public Class<String> getType() {
          return String.class;
        }

        @Override
        public String getLabel() {
          return "string";
        }

        @Override
        public Stream<String> valuesOf(String input, Stream<String> stream)
            throws ValueParseException {
          return Stream.of(valueOf(input));
        }

        @Nullable
        @Override
        public String valueOf(String input, Stream<String> stream) throws ValueParseException {
          return String.valueOf(input);
        }

        @Override
        public String toString(String value) throws StringConversionException {
          return String.valueOf(value);
        }

        @Override
        public int compare(String o1, String o2) {
          return Objects.compare(o1, o2, String::compareTo);
        }
      };

  IConverter<Object> EMPTY =
      new AbstractConverter<Object>() {
        @Override
        public Class<Object> getType() {
          return Object.class;
        }

        @Override
        public String getLabel() {
          return "empty";
        }

        @Override
        public String toString(Object value) throws StringConversionException {
          return "empty";
        }

        @Override
        public Object valueOf(String input, Stream<Object> stream) throws ValueParseException {
          return null;
        }

        @Override
        public Stream<Object> valuesOf(String input, Stream<Object> stream)
            throws ValueParseException {
          return null;
        }

        @Override
        public int compare(Object o1, Object o2) {
          return 0;
        }
      };
}
