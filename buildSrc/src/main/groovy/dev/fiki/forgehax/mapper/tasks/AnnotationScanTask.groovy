package dev.fiki.forgehax.mapper.tasks


import dev.fiki.forgehax.api.mapper.ClassMapping
import dev.fiki.forgehax.api.mapper.FieldMapping
import dev.fiki.forgehax.api.mapper.MappedFormat
import dev.fiki.forgehax.api.mapper.MethodMapping
import dev.fiki.forgehax.mapper.extractor.MapData
import dev.fiki.forgehax.mapper.type.ClassInfo
import dev.fiki.forgehax.mapper.type.FieldInfo
import dev.fiki.forgehax.mapper.type.MethodInfo
import dev.fiki.forgehax.mapper.util.AnnotationValueMap
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

import java.nio.file.Files
import java.nio.file.StandardOpenOption

class AnnotationScanTask extends DefaultTask {
  @Input
  SourceSet targetSourceSet
  @Input
  List<Type> scannedAnnotations = []

  MapData mapper

  @TaskAction
  def action() {
    mapper = getProject().tasks.importSources.data

    targetSourceSet.getOutput().getClassesDirs()
        .filter { it.exists() }
        .each { srcDir ->
          // recurse over every file in directory
          srcDir.eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith('.class')) readClass(it)
          }
        }
  }

  void readClass(File file) {
    final classPath = file.toPath()
    def buffer = Files.readAllBytes(classPath)

    final cn = new ClassNode()
    final cr = new ClassReader(buffer)

    cr.accept(cn, 0)

    // only do a deep scan on certain classes
    if (cn.visibleAnnotations.find { shouldScanClass(it) }) {
      // visit the class mapping annotation and set it to the parent class (if it exists)
      def parentNode = cn.visibleAnnotations?.find { it.desc == Type.getDescriptor(ClassMapping) }
      parentNode?.with { this.restructureAnnotation(it, null) }

      // scan the remaining annotations
      cn.visibleAnnotations?.findAll { it.desc != Type.getDescriptor(ClassMapping) }?.each {
        restructureAnnotation(it, parentNode)
      }

      // visit field annotations
      cn.fields.each { it.visibleAnnotations?.each { restructureAnnotation(it, parentNode) } }

      // visit method and parameter annotations
      cn.methods.each {
        it.visibleAnnotations?.each { restructureAnnotation(it, parentNode) }
        it.visibleParameterAnnotations?.each { it?.each { restructureAnnotation(it, parentNode) } }
      }

      final cw = new NoLoadClassWriter(cr, 0)
      cn.accept(cw)

      Files.write(classPath, cw.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    }
  }

  boolean shouldScanClass(AnnotationNode node) {
    return scannedAnnotations.find { node.desc == it.getDescriptor() }
  }

  void restructureAnnotation(AnnotationNode node, AnnotationNode parentNode) {
    def params = new AnnotationValueMap(node.values)

    switch (node.desc) {
      case Type.getDescriptor(ClassMapping):
        def clazz = getClassType(params)

        if (clazz) {
          // replace the class reference so that the class isn't loaded by the jvm
          params.putAsClass('value', void.class)

          def ci = findClass(clazz.getInternalName(), MappedFormat.MAPPED)
          if (ci != null) {
            params.put('_name', ci.name)
            if (ci.obfName != null) {
              params.put('_obfName', ci.obfName)
            }
          } else {
            // unknown class (not minecraft most likely)
            params.put('_name', clazz.getInternalName())
          }
        }
        break
      case Type.getDescriptor(FieldMapping):
        def fieldName = params.remove('value') as String
        if (fieldName) {
          // remove the string at value, but since it has no default I will keep it
          // to prevent unexpected behavior
          params.put('value', '')

          // the format the provided name is in (i.e srg, mcp, or obfuscated)
          def format = getAndUnmapFormat(params)

          // get the parent class that will represent this object
          // then inject a @ClassMapping into this annotation
          def parentClass = getAndSetParentClass(params, parentNode)

          def field = findField(fieldName, parentClass, format)
          if (field) {
            params.put('_name', field.name)
            params.put('_obfName', field.obfName)
            params.put('_srgName', field.srgName)
          } else {
            // provide at least the class -> string name
            params.put('_name', fieldName)
          }
        }
        break
      case Type.getDescriptor(MethodMapping):
        def methodName = params.remove('value') as String
        if (methodName) {
          def format = getAndUnmapFormat(params)
          def parentClass = getAndSetParentClass(params, parentNode)

          def returnType = params.remove('ret') as Type

          if (returnType == null) {
            def retm = params.remove('retm') as AnnotationNode
            returnType = parseClassMapping(retm)
          }

          def argumentTypes = params.remove('args') as Type[]

          if (argumentTypes == null) {
            def argsm = params.remove('argsm') as List<AnnotationNode>
            argumentTypes = argsm?.with { it.collect {parseClassMapping(it)}.toArray(new Type[0]) }
          }

          def method = findMethod(methodName, parentClass, returnType, argumentTypes, format)
          if (method) {
            params.put('_name', method.name)
            params.put('_obfName', method.obfName)
            params.put('_srgName', method.srgName)
            params.put('_descriptor', method.descriptor)
            params.put('_obfDescriptor', method.obfDescriptor)
          } else {
            if (returnType == null) {
              returnType = Type.VOID_TYPE
            }

            if (argumentTypes == null) {
              argumentTypes = [] as Type[]
            }

            // provide at least the class -> string name
            params.put('_name', methodName)
            params.put('_descriptor', Type.getMethodType(returnType, argumentTypes).getDescriptor())
          }
        }
        break
    }
  }

  Type getClassType(AnnotationValueMap params) {
    def clazz = params.remove('value') as Type

    // for classes that dont have public access
    // we allow access via a string
    if (clazz == null) {
      def className = params.remove('className') as String

      if (className != null) {
        clazz = Type.getObjectType(className)
      }
    }

    if (clazz) {
      // some classes will be children of other classes
      def subClass = params.remove('innerClassNames') as String[]
      if (subClass != null) {
        subClass.each { clazz = Type.getObjectType(clazz.getInternalName() + '$' + it) }
      }
    }

    return clazz
  }

  Type parseClassMapping(AnnotationNode an) {
    if (an != null && an.desc == Type.getDescriptor(ClassMapping)) {
      def map = new AnnotationValueMap(an.values)
      return getClassType(map)
    }
    return null
  }

  MappedFormat getAndUnmapFormat(AnnotationValueMap params) {
    def type = params.getAsMappedFormat('format')
    if (type != null) {
      params.remove('format')
      return type;
    }
    return MappedFormat.MAPPED
  }

  String getAndSetParentClass(AnnotationValueMap params, AnnotationNode parent) {
    // use override parent class if provided
    def parentClass = params.remove('parentClass') as Type

    if (parentClass == null) {
      def an = params.remove('parent') as AnnotationNode
      parentClass = parseClassMapping(an)
    }

    if (parentClass != null) {
      // create new class mapping annotation
      parent = new AnnotationNode(Type.getDescriptor(ClassMapping))
      def ci = findClass(parentClass.getInternalName(), MappedFormat.MAPPED)

      parent.visit('value', Type.VOID_TYPE)

      if (ci != null) {
        parent.visit('_name', ci.name)
        if (ci.obfName != null) {
          parent.visit('_obfName', ci.obfName)
        }
      } else {
        parent.visit('_name', parentClass.getInternalName())
      }

      // insert into the annotation
      params.put('_parentClass', parent)

      return parentClass.getInternalName()
    } else {
      // copy the parent node shallowly... it should not be modified
      def values = Collections.unmodifiableList(new ArrayList(parent.values))
      parent = new AnnotationNode(parent.desc)
      parent.values = values

      // insert into the annotation
      params.put('_parentClass', parent)

      return new AnnotationValueMap(parent.values).get('_name') as String
    }
  }

  ClassInfo findClass(String className, MappedFormat format) {
    switch (format) {
      case MappedFormat.MAPPED:
      case MappedFormat.SRG:
        return mapper.classMap.get(className)
      case MappedFormat.OBFUSCATED:
        return mapper.classMap.values().find { it.obfName == className }
    }
    return null
  }

  FieldInfo[] findFields(String name, String parentClass, MappedFormat from) {
    switch (from) {
      case MappedFormat.MAPPED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED field lookup')
        return mapper.classMap.get(parentClass)?.srgToField?.values()
            ?.findAll { it.name == name }
      case MappedFormat.OBFUSCATED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED field lookup')
        return mapper.classMap.get(parentClass)?.srgToField?.values()
            ?.findAll { it.obfName == name }
      case MappedFormat.SRG:
        return [mapper.srgToField.get(name)] as FieldInfo[]
    }
    return null
  }

  FieldInfo findField(String name, String parentClass, MappedFormat from) {
    def fs = findFields(name, parentClass, from)
    def shouldError = mapper.classMap.containsKey(parentClass)
    if (!fs || fs.length == 0) {
      if (shouldError) println("Found 0 matches for ${parentClass}.${name}!")
    } else if (fs.length > 1) {
      if (shouldError) println("Too many matches for ${parentClass}.${name}!")
    } else {
      return fs[0]
    }
    return null
  }

  MethodInfo[] findMethods(String name, String parentClass, Type retType, Type[] argTypes, MappedFormat from) {
    // since the descriptor is generated from a list of class types, it wont be obfuscated
    switch (from) {
      case MappedFormat.MAPPED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED method lookup')
        return mapper.classMap.get(parentClass)?.srgToMethod?.values()
            ?.findAll { it.name == name && methodDescriptorsMatch(retType, argTypes, it.descriptor) }
      case MappedFormat.OBFUSCATED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED method lookup')
        return mapper.classMap.get(parentClass)?.srgToMethod?.values()
            ?.findAll { it.obfName == name && methodDescriptorsMatch(retType, argTypes, it.descriptor) }
      case MappedFormat.SRG:
        return [mapper.srgToField.get(name)] as MethodInfo[]
    }
    return null
  }

  MethodInfo findMethod(String name, String parentClass, Type retType, Type[] argTypes, MappedFormat from) {
    def fs = findMethods(name, parentClass, retType, argTypes, from)
    def shouldError = mapper.classMap.containsKey(parentClass)
    if (!fs || fs.length == 0) {
      if (shouldError) System.err.println("Found 0 matches for ${parentClass}.${name}!")
    } else if (fs.length > 1) {
      if (shouldError) System.err.println("Too many matches for ${parentClass}.${name}!")
    } else {
      return fs[0]
    }
    return null
  }

  boolean methodDescriptorsMatch(Type retType, Type[] argTypes, String againstDescriptor) {
    if (againstDescriptor == null) {
      // missing data (this shouldn't happen)
      return false
    } else if (!retType && !argTypes) {
      // if no primary descriptor is supplied, then we will skip this check and mark it as good
      return true
    } else {
      def other = Type.getMethodType(againstDescriptor)
      if (retType && argTypes) {
        // full descriptor match test
        return Type.getMethodType(retType, argTypes) == other
      } else if (retType) {
        // only check return type
        return retType == other.getReturnType()
      } else {
        // only check argument types
        return Arrays.equals(argTypes, other.getArgumentTypes())
      }
    }
  }

  static class NoLoadClassWriter extends ClassWriter {
    NoLoadClassWriter(ClassReader classReader, int flags) {
      super(classReader, flags)
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
      return "java/lang/Object"
    }
  }
}
