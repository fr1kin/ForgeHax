package dev.fiki.javac.plugin.map;

import dev.fiki.forgehax.api.asm.runtime.Format;

import java.util.stream.Stream;

public class FieldInfo {
  public final ClassInfo parentClass;
  public String name;
  public String obfName;
  public String srgName;

  public FieldInfo(ClassInfo parentClass) {
    this.parentClass = parentClass;
  }

  public Stream<FieldEntry> getFormats() {
    return Stream.of(
        new FieldEntry(name, Format.NORMAL),
        new FieldEntry(obfName, Format.OBFUSCATED),
        new FieldEntry(srgName, Format.SRG));
  }

  public static class FieldEntry {
    public final String name;
    public final Format format;

    public FieldEntry(String name, Format format) {
      this.name = name;
      this.format = format;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      FieldEntry that = (FieldEntry) o;

      return name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
}
