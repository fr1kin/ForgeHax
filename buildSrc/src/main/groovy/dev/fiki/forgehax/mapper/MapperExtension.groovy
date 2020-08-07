package dev.fiki.forgehax.mapper

import dev.fiki.forgehax.api.mapper.ClassMapping
import dev.fiki.forgehax.api.mapper.MappingScan
import dev.fiki.forgehax.mapper.tasks.AnnotationScanTask
import dev.fiki.forgehax.mapper.tasks.ImportSourcesTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.objectweb.asm.Type

class MapperExtension {
  final Project project;

  ImportSourcesTask importSourcesTask
  List<Type> targetAnnotations = [Type.getType(ClassMapping), Type.getType(MappingScan)]

  MapperExtension(Project project) {
    this.project = project

    importSourcesTask = project.tasks.create("importSources", ImportSourcesTask)

    project.afterEvaluate {
      importSourcesTask.dependsOn project.tasks.extractSrg, project.tasks.createMcpToSrg, project.tasks.downloadMcpConfig
    }

    // add the api classes to the projects build
    project.tasks.jar.from project.fileTree('buildSrc/build/classes/java/api')
  }

  void dependencyOnly(SourceSet sourceSet) {
    // add the api dependency to the project
    project.dependencies.add(sourceSet.getCompileConfigurationName(),
        project.files('buildSrc/build/libs/buildSrc-api.jar'))
  }

  void annotatedWith(String internalClassName) {
    targetAnnotations.add(Type.getObjectType(internalClassName))
  }

  void include(SourceSet sourceSet, String packageName) {
    final compileTask = project.tasks.find { it.name == sourceSet.getCompileJavaTaskName() }

    // add the api dependency to the project
    dependencyOnly(sourceSet)

    // create new task
    def scanTask = project.tasks.create("${sourceSet.name}AnnotationScanner", AnnotationScanTask) {
      targetSourceSet sourceSet
      scannedAnnotations += targetAnnotations
    }

    // run this task after compiling the source
    compileTask.finalizedBy scanTask

    project.afterEvaluate {
      // we need these resources
      scanTask.dependsOn importSourcesTask
    }
  }

  void include(SourceSet sourceSet) {
    include(sourceSet, "")
  }

}
