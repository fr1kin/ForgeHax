package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.forgehax.api.asm.runtime.RtMapField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public interface ASMField {
  static ASMField from(@NonNull RtMapField map) {
    return new Primary(map);
  }

  ASMClass getParentClass();

  String getName();

  Type getTypeDescriptor();

  Format getFormat();

  ASMField setFormat(@NonNull Format format);

  Stream<? extends ASMField> getDelegates();

  default boolean anyNameEquals(String other) {
    return getDelegates()
        .map(ASMField::getName)
        .anyMatch(other::equals);
  }

  abstract class Base implements ASMField {
    @Override
    public String toString() {
      return getParentClass() + "." + getName() + " " + getTypeDescriptor();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof ASMField) {
        ASMField other = (ASMField) obj;
        return this.getParentClass().equals(other.getParentClass())
            && this.getName().equals(other.getName()); // we don't have to check the type
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(getParentClass(), getName());
    }
  }

  @Getter
  final class Primary extends Base {
    private final ASMClass parentClass;
    private final Type typeDescriptor;
    @Getter(AccessLevel.NONE)
    private final Delegate[] delegates;

    Primary(RtMapField map) {
      this.parentClass = ASMClass.from(map.parent());
      this.typeDescriptor = Type.getType(map.typeDescriptor());

      this.delegates = new Delegate[Format.values().length];
      this.delegates[Format.NORMAL.ordinal()] = new Delegate(this, map.name(), Format.NORMAL);

      for (RtMapField.Alternative alt : map.alternatives()) {
        this.delegates[alt.format().ordinal()] = new Delegate(this, alt.name(), alt.format());
      }
    }

    @Override
    public String getName() {
      return setFormat(getFormat()).getName();
    }

    @Override
    public Format getFormat() {
      return ASMEnv.getCurrentClassFormat();
    }

    @Override
    public ASMField setFormat(@NonNull Format format) {
      return delegates[format.ordinal()];
    }

    @Override
    public Stream<? extends ASMField> getDelegates() {
      return Arrays.stream(delegates).filter(Objects::nonNull);
    }
  }

  @Getter
  @RequiredArgsConstructor
  final class Delegate extends Base {
    private final Primary owner;
    private final String name;
    private final Format format;

    @Override
    public ASMClass getParentClass() {
      return owner.getParentClass();
    }

    @Override
    public Type getTypeDescriptor() {
      return owner.getTypeDescriptor();
    }

    @Override
    public ASMField setFormat(@NonNull Format format) {
      return owner.setFormat(format);
    }

    @Override
    public Stream<? extends ASMField> getDelegates() {
      return owner.getDelegates();
    }
  }
}
