package dev.fiki.forgehax.mapper


import dev.fiki.forgehax.api.mapper.MappedFormat
import dev.fiki.forgehax.mapper.extractor.MapData
import dev.fiki.forgehax.mapper.tasks.AnnotationScanTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

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
        data.importSrg(extractSrg.output as File, MappedFormat.OBFUSCATED, MappedFormat.SRG, MappedFormat.OBFUSCATED)
      }

      createMcpToSrg.doLast {
        data.importSrg(createMcpToSrg.output as File, MappedFormat.MAPPED, MappedFormat.SRG, MappedFormat.SRG)
      }

      downloadMcpConfig.doLast {
        // import the file that tells us which methods are static
        def zip = new ZipFile(downloadMcpConfig.output as File)
        zip.entries().with {
          while (it.hasMoreElements()) {
            def e = it.nextElement()
            if ('config/static_methods.txt' == e.getName()) {
              def tmp = File.createTempFile("temp", null)
              tmp.deleteOnExit()

              zip.getInputStream(e).withCloseable { Files.copy(it, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING) }

              data.importStaticMethods(tmp)
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

    // add the api dependency to the project
    project.dependencies.add(sourceSet.getCompileConfigurationName(),
        project.files('buildSrc/build/libs/buildSrc-api.jar'))

    // create new task
    def scanTask = project.tasks.create("${sourceSet.name}AnnotationScanner", AnnotationScanTask) {
      mapper data
      targetSourceSet sourceSet
    }

    compileTask.finalizedBy scanTask

    project.afterEvaluate {
      scanTask.dependsOn extractSrg, createMcpToSrg, downloadMcpConfig
    }
  }

  void include(SourceSet sourceSet) {
    include(sourceSet, "")
  }

}
