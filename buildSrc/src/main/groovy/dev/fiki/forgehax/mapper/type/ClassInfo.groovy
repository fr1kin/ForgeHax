package dev.fiki.forgehax.mapper.type

class ClassInfo {
  String name, obfName;
  Map<String, MethodInfo> srgToMethod = new HashMap<>()
  Map<String, FieldInfo> srgToField = new HashMap<>()

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
