package dev.fiki.javac.plugin.map;

import dev.fiki.forgehax.api.asm.runtime.Format;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ClassInfo {
  public final Map<String, MethodInfo> srgToMethod = new HashMap<>();
  public final Map<String, FieldInfo> srgToField = new HashMap<>();

  public String name;
  public String obfName;

  public Stream<ClassEntry> getFormats() {
    return Stream.of(new ClassEntry(name, Format.NORMAL), new ClassEntry(obfName, Format.OBFUSCATED));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClassInfo classInfo = (ClassInfo) o;

    return name.equals(classInfo.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public static class ClassEntry {
    public final String name;
    public final Format format;

    public ClassEntry(String name, Format format) {
      this.name = name;
      this.format = format;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ClassEntry classEntry = (ClassEntry) o;

      return name.equals(classEntry.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
}
