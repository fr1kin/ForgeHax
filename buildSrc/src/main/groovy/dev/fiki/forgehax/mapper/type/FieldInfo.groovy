package dev.fiki.forgehax.mapper.type

class FieldInfo {
  ClassInfo parent
  String name, obfName, srgName
  boolean isStatic = false

  void setNameByFormat(String name, MappedFormat format) {
    switch (format) {
      case MappedFormat.MAPPED:
        this.name = name
        break
      case MappedFormat.SRG:
        this.srgName = name
        break
      case MappedFormat.OBFUSCATED:
        this.obfName = name
        break
    }
  }

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    FieldInfo fieldInfo = (FieldInfo) o

    if (srgName != fieldInfo.srgName) return false

    return true
  }

  int hashCode() {
    return srgName.hashCode()
  }
}
