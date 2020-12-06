package dev.fiki.forgehax.mapper.extractor

import dev.fiki.forgehax.mapper.type.ClassInfo
import dev.fiki.forgehax.mapper.type.FieldInfo
import dev.fiki.forgehax.mapper.type.MappedFormat
import dev.fiki.forgehax.mapper.type.MethodInfo

class TSrgImporter implements Importer {
  final File file
  final MappedFormat leftFormat, rightFormat

  TSrgImporter(File file, MappedFormat leftFormat, MappedFormat rightFormat) {
    this.file = Objects.requireNonNull(file, 'missing tsrg file')
    this.leftFormat = leftFormat
    this.rightFormat = rightFormat
  }

  @Override
  void read(MapData data) {
    file.newReader().withCloseable {
      ClassInfo lastClass = null

      final scanner = new Scanner(it)
      while (scanner.hasNextLine()) {
        def line = scanner.nextLine()

        // skip empty lines
        if (line.isEmpty() || line.isAllWhitespace()) continue

        if (line.charAt(0).isWhitespace()) {
          // whitespace at first char means this is a member of a class structure
          // must be a field or method

          // skip the first whitespace
          // a space character is used as a delimiter
          def ss = line.substring(1).split('\\s')
          if (ss.length == 2) {
            // if there are only two, then this must be a FIELD
            def key = getSrgKey(ss[0], ss[1])
            def field = data.srgToField.computeIfAbsent(key, { k -> new FieldInfo() })

            field.setNameByFormat(ss[0], leftFormat)
            field.setNameByFormat(ss[1], rightFormat)

            if (lastClass) {
              field.parent = lastClass
              lastClass.srgToField.putIfAbsent(field.srgName, field)
            }
          } else if (ss.length == 3) {
            // if there are three, then this must be a METHOD
            def key = getSrgKey(ss[0], ss[2])
            def method = data.srgToMethod.computeIfAbsent(key, { k -> new MethodInfo() })

            method.setNameByFormat(ss[0], leftFormat)
            method.setNameByFormat(ss[2], rightFormat)
            method.setDescriptorByFormat(ss[1], leftFormat)

            if (lastClass) {
              method.parent = lastClass
              lastClass.srgToMethod.putIfAbsent(method.srgName, method)
            }
          } else {
            println "Could not parse line \"${line}\""
          }
        } else {
          // this must be a class
          def ss = line.split('\\s')
          if (ss.length == 2) {
            def key = getSrgKey(ss[0], ss[1])
            def clazz = data.classMap.computeIfAbsent(key, { k -> new ClassInfo() })

            clazz.setNameByFormat(ss[0], leftFormat)
            clazz.setNameByFormat(ss[1], rightFormat)

            lastClass = clazz
          } else {
            println "Could not parse line \"${line}\""
          }
        }
      }
    }
  }

  String getSrgKey(String left, String right) {
    if (leftFormat == MappedFormat.SRG) {
      return left
    } else if (rightFormat == MappedFormat.SRG) {
      return right
    } else {
      throw new Error('Unable to extract a srg name from left or right string')
    }
  }
}
