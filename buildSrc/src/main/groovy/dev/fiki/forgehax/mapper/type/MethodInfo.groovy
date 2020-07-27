package dev.fiki.forgehax.mapper.type

class MethodInfo {
  ClassInfo parent
  String name, obfName, srgName
  String descriptor, obfDescriptor
  boolean isStatic = false

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    MethodInfo that = (MethodInfo) o

    if (descriptor != that.descriptor) return false
    if (srgName != that.srgName) return false

    return true
  }

  int hashCode() {
    int result
    result = srgName.hashCode()
    result = 31 * result + descriptor.hashCode()
    return result
  }
}
