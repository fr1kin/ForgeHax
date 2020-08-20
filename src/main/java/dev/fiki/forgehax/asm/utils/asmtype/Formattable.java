package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.mapper.MappedFormat;

import java.util.stream.Stream;

interface Formattable<T> {
  default MappedFormat getFormat() {
    return MappedFormat.MAPPED;
  }

  default void setFormat(MappedFormat format) {
  }

  default T format(MappedFormat format) {
    return (T) this;
  };

  default Stream<T> stream() {
    return Stream.of((T) this);
  };
}
