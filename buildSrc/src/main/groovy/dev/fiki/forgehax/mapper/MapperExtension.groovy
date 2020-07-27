package dev.fiki.forgehax.mapper

import dev.fiki.forgehax.api.mapper.*
import dev.fiki.forgehax.mapper.extractor.MinecraftMappings
import dev.fiki.forgehax.mapper.type.FieldInfo
import dev.fiki.forgehax.mapper.type.MethodInfo
import dev.fiki.forgehax.mapper.util.AnnotationValueList
import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile

class MapperExtension {
  static final mcp = new MinecraftMappings()
  final Project project;

  def extractSrg, createMcpToSrg, downloadMcpConfig

  MapperExtension(Project project) {
    this.project = project

    project.afterEvaluate {
      // get the mapping zip
//      final mappingsZip = project.configurations.compile.resolvedConfiguration.resolvedArtifacts.find { it.name.startsWith('mappings_')}

      // get the obfuscated -> srg file
      extractSrg = project.tasks.find { 'extractSrg' == it.name }
      // get the srg -> mcp file
      createMcpToSrg = project.tasks.find { 'createMcpToSrg' == it.name }
      // get the zip that contains info about static methods
      downloadMcpConfig = project.tasks.find { 'downloadMcpConfig' == it.name }

      Objects.requireNonNull(extractSrg, 'extractSrg')
      Objects.requireNonNull(createMcpToSrg, 'createMcpToSrg')
      Objects.requireNonNull(downloadMcpConfig, 'downloadMcpConfig')

      extractSrg.doLast {
        mcp.importSrg(extractSrg.output, MappedFormat.OBFUSCATED, MappedFormat.SRG, MappedFormat.OBFUSCATED)
      }

      createMcpToSrg.doLast {
        mcp.importSrg(createMcpToSrg.output, MappedFormat.MAPPED, MappedFormat.SRG, MappedFormat.SRG)
      }

//      requireFileExists(createMcpToSrg.output)
//      requireFileExists(extractSrg.output)
//      requireFileExists(downloadMcpConfig.output)
//
//      // import the mcp, srg, and obfuscated mappings
//      mcp.importSrg(createMcpToSrg.output, MappedFormat.MAPPED, MappedFormat.SRG, MappedFormat.SRG)
//      mcp.importSrg(extractSrg.output, MappedFormat.OBFUSCATED, MappedFormat.SRG, MappedFormat.OBFUSCATED)

      downloadMcpConfig.doLast {
        // import the file that tells us which methods are static
        def zip = new ZipFile(downloadMcpConfig.output)
        zip.entries().with {
          while (it.hasMoreElements()) {
            def e = it.nextElement()
            if ('config/static_methods.txt' == e.getName()) {
              def tmp = File.createTempFile("temp", null)
              tmp.deleteOnExit()

              zip.getInputStream(e).withCloseable { Files.copy(it, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING) }

              mcp.importStaticMethods(tmp)
              return
            }
          }
          throw new Error('Cannot find static method file!')
        }
      }
    }

    // add the api classes to the projects build
    project.tasks.jar.from project.fileTree('buildSrc/build/classes/java/api')
  }

  void include(SourceSet sourceSet, String packageName) {
    final compileTask = project.tasks.find { it.name == sourceSet.getCompileJavaTaskName() }

    // add the api classes to the classpath
//    sourceSet.compileClasspath += project.fileTree('buildSrc/build/classes/java/api')
//    sourceSet.runtimeClasspath += project.fileTree('buildSrc/build/classes/java/api')

    // add the api dependency to the project
    project.dependencies.add(sourceSet.getCompileConfigurationName(),
        project.files('buildSrc/build/libs/buildSrc-api.jar'))

    project.afterEvaluate {
      compileTask.mustRunAfter extractSrg, createMcpToSrg, downloadMcpConfig
    }

    // run this task after compile
    compileTask.doLast {
      // get the compiled classes
      sourceSet.getOutput().getClassesDirs()
          .filter { it.exists() }
          .each { srcDir ->
            // recurse over every file in directory
            srcDir.eachFileRecurse(FileType.FILES) {
              if (it.name.endsWith('.class')) readClass(it)
            }
          }
    }
  }

  void include(SourceSet sourceSet) {
    include(sourceSet, "")
  }

  static void requireFileExists(File file) {
    Objects.requireNonNull(file, 'file')
    if (!file.exists()) throw new Error("File '${file.name}' does not exist!")
  }

  static void readClass(File file) {
    final classPath = file.toPath()
    def buffer = Files.readAllBytes(classPath)

    final cn = new ClassNode()
    final cr = new ClassReader(buffer)
    cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)

    // only do a deep scan on certain classes
    if (cn.visibleAnnotations.find { shouldScanClass(it) }) {
      // visit class annotations
      final ref = ['']
      cn.visibleAnnotations?.each { ref[0] = restructureAnnotation(it, null) }
      final parent = !ref[0]?.isEmpty() ? ref[0] : null

      // visit field annotations
      cn.fields.each { it.visibleAnnotations?.each { restructureAnnotation(it, parent) } }

      // visit method and parameter annotations
      cn.methods.each {
        it.visibleAnnotations?.each { restructureAnnotation(it, parent) }
        it.visibleParameterAnnotations?.each { it?.each { restructureAnnotation(it, parent) } }
      }

      final cw = new NoLoadClassWriter(cr, 0)
      cn.accept(cw)

      Files.write(classPath, cw.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    }
  }

  static String restructureAnnotation(AnnotationNode node, String parent) {
    def params = new AnnotationValueList(node.values)
    switch (node.desc) {
      case Type.getDescriptor(ClassMapping):
        def clazz = params.getAsType('value')
        if (clazz) {
          // some classes will be children of other classes
          def subClass = params.getAsString('subClassName')
          if (subClass) {
            // optional subclass name
            clazz = Type.getObjectType(clazz.getInternalName() + '$' + subClass)
          }

          // replace the class reference so that the class isn't loaded by the jvm
          params.putAsClass('value', void.class)
          params.put('_name', clazz.getInternalName())

          def ci = mcp.classMap.get(clazz.getInternalName())
          if (ci && ci.obfName) {
            params.put('_obfName', ci.obfName)
            return ci.name
          }
        }
        return clazz
      case Type.getDescriptor(FieldMapping):
        def fieldName = params.getAsString('value')
        if (fieldName) {
          // remove the string at value, but since it has no default I will keep it
          // to prevent unexpected behavior
          params.put('value', '')

          def format = checkMappedFormatOverride(params)
          def parentClass = checkParentClassOverride(params, parent)

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
        def methodName = params.getAsString('value')
        if (methodName) {
          // remove the string at value, but since it has no default I will keep it
          // to prevent unexpected behavior
          params.put('value', '')

          def format = checkMappedFormatOverride(params)
          def parentClass = checkParentClassOverride(params, parent)

          def returnType = params.getAsType('ret')
          def argumentTypes = params.getAsTypeArray('args')

          if (returnType) {
            params.remove('ret')
          }

          if (argumentTypes != null) {
            params.remove('args')
          }

          def method = findMethod(methodName, parentClass, returnType, argumentTypes, format)
          if (method) {
            params.put('_name', method.name)
            params.put('_obfName', method.obfName)
            params.put('_srgName', method.srgName)
            params.put('_descriptor', method.descriptor)
            params.put('_obfDescriptor', method.obfDescriptor)
          } else {
            // provide at least the class -> string name
            params.put('_name', methodName)
          }
        }
        break
    }

    return null
  }

  private static boolean shouldScanClass(AnnotationNode node) {
    return node.desc == Type.getDescriptor(ScanMappings) || node.desc == Type.getDescriptor(ClassMapping)
  }

  private static MappedFormat checkMappedFormatOverride(AnnotationValueList params) {
    def type = params.getAsMappedFormat('format')
    if (type) {
      params.remove('format')
      return type;
    }
    return MappedFormat.MAPPED
  }

  private static String checkParentClassOverride(AnnotationValueList params, String currentParent) {
    // use override parent class if provided
    def parentClass = params.getAsType('parentClass')
    if (parentClass && parentClass != Type.VOID_TYPE) {
      // replace to prevent jvm from loading class
      params.putAsClass('parentClass', Type.VOID_TYPE)
      return parentClass.getInternalName()
    }
    return currentParent
  }

  private static FieldInfo[] findFields(String name, String parentClass, MappedFormat from) {
    switch (from) {
      case MappedFormat.MAPPED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED field lookup')
        return mcp.classMap.get(parentClass)?.srgToField?.values()
            ?.findAll { it.name == name }
      case MappedFormat.OBFUSCATED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED field lookup')
        return mcp.classMap.get(parentClass)?.srgToField?.values()
            ?.findAll { it.obfName == name }
      case MappedFormat.SRG:
        return [mcp.srgToField.get(name)] as FieldInfo[]
    }
    return null
  }

  private static FieldInfo findField(String name, String parentClass, MappedFormat from) {
    def fs = findFields(name, parentClass, from)
    if (!fs || fs.length == 0) {
      System.err.println("Found 0 matches for ${parentClass}.${name}!")
    } else if (fs.length > 1) {
      System.err.println("Too many matches for ${parentClass}.${name}!")
    } else {
      return fs[0]
    }
    return null
  }

  private static MethodInfo[] findMethods(String name, String parentClass, Type retType, Type[] argTypes, MappedFormat from) {
    // since the descriptor is generated from a list of class types, it wont be obfuscated
    switch (from) {
      case MappedFormat.MAPPED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED method lookup')
        return mcp.classMap.get(parentClass)?.srgToMethod?.values()
            ?.findAll { it.name == name && methodDescriptorsMatch(retType, argTypes, it.descriptor) }
      case MappedFormat.OBFUSCATED:
        Objects.requireNonNull(parentClass, 'parentClass required for MAPPED method lookup')
        return mcp.classMap.get(parentClass)?.srgToMethod?.values()
            ?.findAll { it.obfName == name && methodDescriptorsMatch(retType, argTypes, it.descriptor) }
      case MappedFormat.SRG:
        return [mcp.srgToField.get(name)] as MethodInfo[]
    }
    return null
  }

  private static MethodInfo findMethod(String name, String parentClass, Type retType, Type[] argTypes, MappedFormat from) {
    def fs = findMethods(name, parentClass, retType, argTypes, from)
    if (!fs || fs.length == 0) {
      System.err.println("Found 0 matches for ${parentClass}::${name}!")
    } else if (fs.length > 1) {
      System.err.println("Too many matches for ${parentClass}::${name}!")
    } else {
      return fs[0]
    }
    return null
  }

  private static boolean methodDescriptorsMatch(Type retType, Type[] argTypes, String againstDescriptor) {
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

  private static class NoLoadClassWriter extends ClassWriter {
    NoLoadClassWriter(ClassReader classReader, int flags) {
      super(classReader, flags)
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
      return "java/lang/Object"
    }
  }
}
