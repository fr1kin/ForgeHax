package dev.fiki.forgehax.mapper


import dev.fiki.forgehax.api.mapper.MappedFormat
import dev.fiki.forgehax.mapper.extractor.MapData
import dev.fiki.forgehax.mapper.extractor.StaticMethodsImporter
import dev.fiki.forgehax.mapper.extractor.TSrgImporter
import dev.fiki.forgehax.mapper.tasks.AnnotationScanTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

class MapperExtension {
  final static data = new MapData()
  final Project project;

  def extractSrg, createMcpToSrg, downloadMcpConfig

  MapperExtension(Project project) {
    this.project = project

    project.afterEvaluate {
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
        data.importSource(new TSrgImporter(extractSrg.output as File, MappedFormat.OBFUSCATED, MappedFormat.SRG))
      }

      createMcpToSrg.doLast {
        data.importSource(new TSrgImporter(createMcpToSrg.output as File, MappedFormat.MAPPED, MappedFormat.SRG))
      }

      downloadMcpConfig.doLast {
        data.importSource(StaticMethodsImporter.fromMcpConfigZip(downloadMcpConfig.output as File))
      }
    }

    // add the api classes to the projects build
    project.tasks.jar.from project.fileTree('buildSrc/build/classes/java/api')
  }

  void include(SourceSet sourceSet, String packageName) {
    final compileTask = project.tasks.find { it.name == sourceSet.getCompileJavaTaskName() }

    // add the api dependency to the project
    project.dependencies.add(sourceSet.getCompileConfigurationName(),
        project.files('buildSrc/build/libs/buildSrc-api.jar'))

    // create new task
    def scanTask = project.tasks.create("${sourceSet.name}AnnotationScanner", AnnotationScanTask) {
      mapper data
      targetSourceSet sourceSet
    }

    // run this task after compiling the source
    compileTask.finalizedBy scanTask

    project.afterEvaluate {
      // we need these resources
      scanTask.dependsOn extractSrg, createMcpToSrg, downloadMcpConfig
    }
  }

  void include(SourceSet sourceSet) {
    include(sourceSet, "")
  }

}
