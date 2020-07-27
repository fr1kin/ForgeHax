package dev.fiki.forgehax.mapper.extractor

import dev.fiki.forgehax.api.mapper.MappedFormat
import dev.fiki.forgehax.mapper.type.ClassInfo
import dev.fiki.forgehax.mapper.type.FieldInfo
import dev.fiki.forgehax.mapper.type.MethodInfo


class MinecraftMappings {
  Map<String, ClassInfo> classMap = new HashMap<>()
  Map<String, MethodInfo> srgToMethod = new HashMap<>()
  Map<String, FieldInfo> srgToField = new HashMap<>()

  void importSrg(File file, MappedFormat leftType, MappedFormat rightType, MappedFormat descriptorType) {
    file.newReader().withCloseable {
      def lastClass = null;
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
            def key = leftType == MappedFormat.SRG ? ss[0] : (rightType == MappedFormat.SRG ? ss[1] : null)
            def field = srgToField.computeIfAbsent(key, { k -> new FieldInfo() })

            setNameByType(field, ss[0], leftType)
            setNameByType(field, ss[1], rightType)

            if(lastClass) {
              field.parent = lastClass
              lastClass.srgToField.putIfAbsent(field.srgName, field)
            }
          } else if (ss.length == 3) {
            // if there are three, then this must be a METHOD
            def key = leftType == MappedFormat.SRG ? ss[0] : (rightType == MappedFormat.SRG ? ss[2] : null)
            def method = srgToMethod.computeIfAbsent(key, { k -> new MethodInfo() })

            setNameByType(method, ss[0], leftType)
            setNameByType(method, ss[2], rightType)
            setDescriptorByType(method, ss[1], descriptorType)

            if(lastClass) {
              method.parent = lastClass
              lastClass.srgToMethod.putIfAbsent(method.srgName, method)
            }
          } else {
            println("Could not parse line \"${line}\"")
          }
        } else {
          // this must be a class
          def ss = line.split('\\s')
          if (ss.length == 2) {
            def key = leftType == MappedFormat.SRG ? ss[0] : (rightType == MappedFormat.SRG ? ss[1] : null)
            def clazz = classMap.computeIfAbsent(key, { k -> new ClassInfo() })
            setNameByType(clazz, ss[0], leftType)
            setNameByType(clazz, ss[1], rightType)
            lastClass = clazz;
          } else {
            println("Could not parse line \"${line}\"")
          }
        }
      }
    }
  }

  void importStaticMethods(File staticMethods) {
    staticMethods.newReader().withCloseable {
      final scanner = new Scanner(it)
      while (scanner.hasNextLine()) {
        def methodSrg = scanner.nextLine()
        def method = srgToMethod.get(methodSrg)
        if (method) {
          method.isStatic = true
        }
      }
    }
  }

  static void setNameByType(Object o, String value, MappedFormat type) {
    switch (type) {
      case MappedFormat.MAPPED:
        o.name = value
        break
      case MappedFormat.OBFUSCATED:
        o.obfName = value
        break
      case MappedFormat.SRG:
        if (o instanceof ClassInfo) o.name = value
        else o.srgName = value
    }
  }

  static void setDescriptorByType(MethodInfo o, String value, MappedFormat type) {
    switch (type) {
      case MappedFormat.SRG:
      case MappedFormat.MAPPED:
        o.descriptor = value
        break
      case MappedFormat.OBFUSCATED:
        o.obfDescriptor = value
        break
    }
  }
}
