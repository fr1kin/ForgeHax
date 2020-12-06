package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.forgehax.api.asm.runtime.RtMapClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public interface ASMClass {
  static ASMClass from(@NonNull RtMapClass map) {
    return new Primary(map);
  }

  String getClassName();

  Format getFormat();

  ASMClass setFormat(Format format);

  Stream<? extends ASMClass> getDelegates();

  abstract class Base implements ASMClass {
    @Override
    public String toString() {
      return getClassName();
    }

    @Override
    public boolean equals(Object obj) {
      return obj == this || (obj instanceof ASMClass && this.getClassName().equals(((ASMClass) obj).getClassName()));
    }
  }

  final class Primary extends Base {
    private final Delegate[] delegates;

    Primary(RtMapClass map) {
      this.delegates = new Delegate[Format.values().length];
      this.delegates[Format.NORMAL.ordinal()] = new Delegate(this, map.className(), Format.NORMAL);
      for (RtMapClass.Alternative alt : map.alternatives()) {
        this.delegates[alt.format().ordinal()] = new Delegate(this, alt.className(), alt.format());
      }
    }

    @Override
    public String getClassName() {
      return setFormat(getFormat()).getClassName();
    }

    @Override
    public Format getFormat() {
      return ASMEnv.getCurrentClassFormat();
    }

    @Override
    public ASMClass setFormat(Format format) {
      return delegates[format.ordinal()];
    }

    @Override
    public Stream<? extends Delegate> getDelegates() {
      return Arrays.stream(delegates).filter(Objects::nonNull);
    }
  }

  @Getter
  @RequiredArgsConstructor
  final class Delegate extends Base {
    private final Primary owner;

    @EqualsAndHashCode.Include
    private final String className;

    @EqualsAndHashCode.Exclude
    private final Format format;

    @Override
    public ASMClass setFormat(Format format) {
      return owner.setFormat(format);
    }

    @Override
    public Stream<? extends ASMClass> getDelegates() {
      return owner.getDelegates();
    }
  }
}
