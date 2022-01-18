package dev.fiki.forgehax.mapper


import dev.fiki.forgehax.mapper.tasks.ImportSourcesTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

class MapperExtension {
  final Project project;

  ImportSourcesTask importSourcesTask

  MapperExtension(Project project) {
    this.project = project

    importSourcesTask = project.tasks.create("importSources", ImportSourcesTask)

    project.afterEvaluate {
      importSourcesTask.dependsOn project.tasks.extractSrg, project.tasks.createMcpToSrg, project.tasks.downloadMcpConfig
    }

    // add the api classes to the projects build
    project.tasks.jar.from project.fileTree('buildSrc/Annotations/build/classes/java/main')
  }

  void dependencyOnly(SourceSet sourceSet) {
    final annotationsJar = project.files('buildSrc/Annotations/build/libs/Annotations.jar')
    final pluginJar = project.files('buildSrc/JavacPlugin/build/libs/JavacPlugin.jar')

    project.dependencies.add(sourceSet.implementationConfigurationName, annotationsJar)
    project.dependencies.add(sourceSet.annotationProcessorConfigurationName, annotationsJar)
    project.dependencies.add(sourceSet.compileOnlyConfigurationName, pluginJar)
    project.dependencies.add(sourceSet.annotationProcessorConfigurationName, pluginJar)
  }

  void targets(SourceSet... sourceSets) {
    sourceSets.each { SourceSet sourceSet ->
      // add the api dependency to the project
      dependencyOnly(sourceSet)

      project.tasks.find { it.getName() == sourceSet.getCompileJavaTaskName() }.with {
        Objects.requireNonNull(it, 'Could not find java compile task!').dependsOn importSourcesTask
      }
    }
  }
}
