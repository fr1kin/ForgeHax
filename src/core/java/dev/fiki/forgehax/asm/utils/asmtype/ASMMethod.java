package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.mapper.MappedFormat;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.objectweb.asm.Type;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class ASMMethod implements Formattable<ASMMethod> {
  public static ASMMethod unmap(MethodMapping mapping) {
    return auto(ASMClass.unmap(mapping._parentClass()),
        mapping._name(), mapping._obfName(), mapping._srgName(),
        mapping._descriptor(), mapping._obfDescriptor());
  }

  public static ASMMethod single(ASMClass parentClass, String name, Type descriptor) {
    return new Single(parentClass, name, descriptor);
  }

  public static ASMMethod multi(ASMClass parentClass, String name, String obfName, String srgName,
      Type descriptor, Type obfDescriptor) {
    return new Container(parentClass, name, srgName, obfName, descriptor, obfDescriptor);
  }

  public static ASMMethod auto(ASMClass parentClass, String name, String obfName, String srgName,
      Type descriptor, Type obfDescriptor) {
    name = Util.emptyToNull(name);
    obfName = Util.emptyToNull(obfName);
    srgName = Util.emptyToNull(srgName);

    if (Stream.of(name, obfDescriptor, srgName).filter(Objects::nonNull).count() > 1) {
      return new Container(parentClass, name, srgName, obfName, descriptor, obfDescriptor);
    } else {
      return new Single(parentClass, Util.firstNonNull(name, srgName, obfName), descriptor);
    }
  }

  public static ASMMethod auto(ASMClass parentClass, String name, String obfName, String srgName,
      String descriptor, String obfDescriptor) {
    Type a0 = Util.descriptorToTypeOrNull(descriptor);
    Type a1 = Util.descriptorToTypeOrNull(obfDescriptor);
    return auto(parentClass, name, obfName, srgName, a0, a1);
  }

  public abstract ASMClass getParentClass();

  public abstract String getName();

  public abstract Type getDescriptor();

  public String getDescriptorString() {
    return getDescriptor().getDescriptor();
  }

  public Type getReturnType() {
    return getDescriptor().getReturnType();
  }

  public Type[] getArgumentTypes() {
    return getDescriptor().getArgumentTypes();
  }

  public boolean isNameEqual(String other) {
    return stream().map(ASMMethod::getName).anyMatch(other::equals);
  }

  @Override
  public String toString() {
    return getName() + "::" + getParentClass().getName();
  }

  @Getter
  @EqualsAndHashCode(callSuper = false)
  @RequiredArgsConstructor
  static private class Single extends ASMMethod {
    private final ASMClass parentClass;
    private final String name;
    private final Type descriptor;
  }

  @Getter
  @RequiredArgsConstructor
  static private class Multi extends ASMMethod {
    private final ASMMethod parent;
    private final String name;
    private final Type descriptor;
    private final MappedFormat format;

    @Override
    public ASMClass getParentClass() {
      return getParent().getParentClass().format(format);
    }

    @Override
    public ASMMethod format(MappedFormat format) {
      return getParent().format(format);
    }

    @Override
    public Stream<ASMMethod> stream() {
      return getParent().stream();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Multi multi = (Multi) o;

      if (!getParentClass().equals(multi.getParentClass())) return false;
      if (!name.equals(multi.name)) return false;
      return descriptor.equals(multi.descriptor);
    }

    @Override
    public int hashCode() {
      int result = getParentClass().hashCode();
      result = 31 * result + name.hashCode();
      result = 31 * result + descriptor.hashCode();
      return result;
    }
  }

  static private class Container extends ASMMethod {
    @Getter
    private final ASMClass parentClass;
    private final ASMMethod mapped;
    private final ASMMethod obfuscated;
    private final ASMMethod searge;

    @Setter
    @Getter
    private MappedFormat format = MappedFormat.SRG;

    Container(ASMClass parentClass, String name, String srgName, String obfName, Type descriptor, Type obfDescriptor) {
      this.parentClass = parentClass;

      this.mapped = name != null
          ? new Multi(this, name, Objects.requireNonNull(descriptor), MappedFormat.MAPPED)
          : null;
      this.obfuscated = obfName != null
          ? new Multi(this, obfName, Objects.requireNonNull(obfDescriptor), MappedFormat.OBFUSCATED)
          : null;
      this.searge = srgName != null
          ? new Multi(this, srgName, Objects.requireNonNull(descriptor), MappedFormat.SRG)
          : null;

      if (this.mapped == null && this.obfuscated == null && this.searge == null) {
        throw new IllegalArgumentException("No valid method information provided");
      }
    }

    @Override
    public String getName() {
      return format(getFormat()).getName();
    }

    @Override
    public Type getDescriptor() {
      return format(getFormat()).getDescriptor();
    }

    @Override
    public ASMMethod format(MappedFormat format) {
      switch (format) {
        case MAPPED:
          return Util.firstNonNull(mapped, searge, obfuscated);
        case SRG:
          return Util.firstNonNull(searge, mapped, obfuscated);
        case OBFUSCATED:
          return Util.firstNonNull(obfuscated, searge, mapped);
      }
      throw new IllegalStateException();
    }

    @Override
    public Stream<ASMMethod> stream() {
      return Stream.of(mapped, searge, obfuscated).filter(Objects::nonNull);
    }

    @Override
    public String toString() {
      return stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Container container = (Container) o;

      if (!parentClass.equals(container.parentClass)) return false;
      if (mapped != null ? !mapped.equals(container.mapped) : container.mapped != null) return false;
      if (obfuscated != null ? !obfuscated.equals(container.obfuscated) : container.obfuscated != null) return false;
      return searge != null ? searge.equals(container.searge) : container.searge == null;
    }

    @Override
    public int hashCode() {
      int result = parentClass.hashCode();
      result = 31 * result + (mapped != null ? mapped.hashCode() : 0);
      result = 31 * result + (obfuscated != null ? obfuscated.hashCode() : 0);
      result = 31 * result + (searge != null ? searge.hashCode() : 0);
      return result;
    }
  }
}
