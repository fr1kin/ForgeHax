package dev.fiki.forgehax.mapper.type

class ClassInfo {
  String name, obfName;
  Map<String, MethodInfo> srgToMethod = new HashMap<>()
  Map<String, FieldInfo> srgToField = new HashMap<>()

  void setNameByFormat(String name, MappedFormat format) {
    switch (format) {
      case MappedFormat.MAPPED:
      case MappedFormat.SRG:
        this.name = name
        break
      case MappedFormat.OBFUSCATED:
        this.obfName = name
        break
    }
  }

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    ClassInfo classInfo = (ClassInfo) o

    if (name != classInfo.name) return false

    return true
  }

  int hashCode() {
    return name.hashCode()
  }
}
