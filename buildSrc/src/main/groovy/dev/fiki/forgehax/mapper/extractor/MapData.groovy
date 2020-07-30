package dev.fiki.forgehax.mapper.extractor

import dev.fiki.forgehax.mapper.type.ClassInfo
import dev.fiki.forgehax.mapper.type.FieldInfo
import dev.fiki.forgehax.mapper.type.MethodInfo

class MapData {
  Map<String, ClassInfo> classMap = new HashMap<>()
  Map<String, MethodInfo> srgToMethod = new HashMap<>()
  Map<String, FieldInfo> srgToField = new HashMap<>()

  void importSource(Importer importer) {
    importer.read(this)
  }
}
