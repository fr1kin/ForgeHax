package dev.fiki.forgehax.mapper.tasks

import dev.fiki.forgehax.api.mapper.MappedFormat
import dev.fiki.forgehax.mapper.extractor.MapData
import dev.fiki.forgehax.mapper.extractor.StaticMethodsImporter
import dev.fiki.forgehax.mapper.extractor.TSrgImporter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ImportSourcesTask extends DefaultTask {
  MapData data = new MapData()

  @TaskAction
  void action() {
    def project = getProject()
    data.importSource(new TSrgImporter(project.tasks.extractSrg.output as File, MappedFormat.OBFUSCATED, MappedFormat.SRG))
    data.importSource(new TSrgImporter(project.tasks.createMcpToSrg.output as File, MappedFormat.MAPPED, MappedFormat.SRG))
    data.importSource(StaticMethodsImporter.fromMcpConfigZip(project.tasks.downloadMcpConfig.output as File))
  }
}
