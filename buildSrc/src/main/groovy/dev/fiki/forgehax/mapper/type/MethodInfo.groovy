package dev.fiki.forgehax.mapper.type

class MethodInfo {
  ClassInfo parent
  String name, obfName, srgName
  String descriptor, obfDescriptor
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

  void setDescriptorByFormat(String descriptor, MappedFormat format) {
    switch (format) {
      case MappedFormat.MAPPED:
      case MappedFormat.SRG:
        this.descriptor = descriptor
        break
      case MappedFormat.OBFUSCATED:
        this.obfDescriptor = descriptor
        break
    }
  }

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
