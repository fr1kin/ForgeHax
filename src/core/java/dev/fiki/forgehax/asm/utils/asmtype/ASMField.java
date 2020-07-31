package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MappedFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class ASMField implements Formattable<ASMField> {
  public static ASMField unmap(@NonNull FieldMapping mapping) {
    return auto(ASMClass.unmap(mapping._parentClass()),
        mapping._name(), mapping._srgName(), mapping._obfName());
  }

  public static ASMField single(ASMClass parentClass, String name) {
    return new Single(parentClass, name);
  }

  public static ASMField multi(ASMClass parentClass, String name, String srgName, String obfName) {
    return new Container(parentClass, name, obfName, srgName);
  }

  public static ASMField auto(ASMClass parentClass, String name, String srgName, String obfName) {
    name = Util.emptyToNull(name);
    srgName = Util.emptyToNull(srgName);
    obfName = Util.emptyToNull(obfName);
    if (Stream.of(name, obfName, srgName).filter(Objects::nonNull).count() > 1) {
      return multi(parentClass, name, obfName, srgName);
    } else {
      return single(parentClass, Util.firstNonNull(name, obfName, srgName));
    }
  }

  public abstract ASMClass getParentClass();

  public abstract String getName();

  public boolean isNameEqual(String other) {
    return stream().map(ASMField::getName).anyMatch(other::equals);
  }

  @Override
  public String toString() {
    return getName() + "." + getParentClass().getName();
  }

  @Getter
  @RequiredArgsConstructor
  static private class Single extends ASMField {
    private final ASMClass parentClass;
    private final String name;
  }

  @Getter
  @RequiredArgsConstructor
  static private class Multi extends ASMField {
    private final ASMField parent;
    private final String name;
    private final MappedFormat format;

    @Override
    public ASMClass getParentClass() {
      return getParent().getParentClass().format(format);
    }

    @Override
    public ASMField format(MappedFormat format) {
      return getParent().format(format);
    }

    @Override
    public Stream<ASMField> stream() {
      return getParent().stream();
    }
  }

  static private class Container extends ASMField {
    @Getter
    private final ASMClass parentClass;
    private final ASMField mapped;
    private final ASMField srg;
    private final ASMField obfuscated;

    @Getter
    @Setter
    private MappedFormat format = MappedFormat.SRG;

    public Container(@NonNull ASMClass parentClass, String name, String srgName, String obfName) {
      this.parentClass = parentClass;

      this.mapped = name != null ? new Multi(this, name, MappedFormat.MAPPED) : null;
      this.srg = srgName != null ? new Multi(this, srgName, MappedFormat.SRG) : null;
      this.obfuscated = obfName != null ? new Multi(this, obfName, MappedFormat.OBFUSCATED) : null;

      if (this.mapped == null && this.srg == null && this.obfuscated == null) {
        throw new IllegalArgumentException("All mappings are null");
      }
    }

    @Override
    public String getName() {
      return format(getFormat()).getName();
    }

    @Override
    public ASMField format(MappedFormat format) {
      switch (format) {
        case MAPPED:
          return Util.firstNonNull(mapped, srg, obfuscated);
        case SRG:
          return Util.firstNonNull(srg, mapped, obfuscated);
        case OBFUSCATED:
          return Util.firstNonNull(obfuscated, srg, mapped);
      }
      throw new IllegalStateException();
    }

    @Override
    public Stream<ASMField> stream() {
      return Stream.of(mapped, srg, obfuscated).filter(Objects::nonNull);
    }

    @Override
    public String toString() {
      return stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));
    }
  }
}
