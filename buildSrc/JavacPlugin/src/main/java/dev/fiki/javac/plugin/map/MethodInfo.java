package dev.fiki.javac.plugin.map;

import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.javac.plugin.type.TypeRef;

import java.util.stream.Stream;

public class MethodInfo {
  public final ClassInfo parentClass;
  public String name;
  public String obfName;
  public String srgName;
  public TypeRef descriptor;
  public TypeRef obfDescriptor;

  public MethodInfo(ClassInfo parentClass) {
    this.parentClass = parentClass;
  }

  public Stream<MethodEntry> getFormats() {
    return Stream.of(
        new MethodEntry(name, descriptor, Format.NORMAL),
        new MethodEntry(obfName, obfDescriptor, Format.OBFUSCATED),
        new MethodEntry(srgName, descriptor, Format.SRG)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MethodInfo methodInfo = (MethodInfo) o;

    if (!parentClass.equals(methodInfo.parentClass)) return false;
    if (!srgName.equals(methodInfo.srgName)) return false;
    return descriptor.equals(methodInfo.descriptor);
  }

  @Override
  public int hashCode() {
    int result = parentClass.hashCode();
    result = 31 * result + srgName.hashCode();
    result = 31 * result + descriptor.hashCode();
    return result;
  }

  public static class MethodEntry {
    public final String name;
    public final TypeRef descriptor;
    public final Format format;

    public MethodEntry(String name, TypeRef descriptor, Format format) {
      this.name = name;
      this.descriptor = descriptor;
      this.format = format;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MethodEntry that = (MethodEntry) o;

      if (!name.equals(that.name)) return false;
      return descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + descriptor.hashCode();
      return result;
    }
  }
}
