package dev.fiki.forgehax.mapper.tasks

import dev.fiki.forgehax.mapper.extractor.MapData
import dev.fiki.forgehax.mapper.extractor.StaticMethodsImporter
import dev.fiki.forgehax.mapper.extractor.TSrgImporter
import dev.fiki.forgehax.mapper.type.MappedFormat
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class ImportSourcesTask extends DefaultTask {
  @Internal
  MapData data = new MapData()
  @OutputFile
  File output = new File(project.buildDir, 'minecraft.map')

  @TaskAction
  void action() {
    def project = getProject()
    data.importSource(new TSrgImporter(project.tasks.extractSrg.output.getAsFile().get(), MappedFormat.OBFUSCATED, MappedFormat.SRG))
    data.importSource(new TSrgImporter(project.tasks.createMcpToSrg.output.getAsFile().get(), MappedFormat.MAPPED, MappedFormat.SRG))
    data.importSource(StaticMethodsImporter.fromMcpConfigZip(project.tasks.downloadMcpConfig.output.getAsFile().get()))

    def builder = new StringBuilder()
    data.classMap.values()
        .each {
          builder.append(it.name)
              .append(' ')
              .append(it.obfName)
              .append('\n')

          it.srgToField.values()
              .each {
                builder.append('\t')
                    // if the field is static use FS (Field Static) or F for just Field
                    .append(it.isStatic ? 'FS' : 'F')
                    .append(' ')
                    .append(firstNonNull(it.name, it.srgName))
                    .append(' ')
                    .append(firstNonNull(it.obfName, 'null'))
                    .append(' ')
                    .append(firstNonNull(it.srgName, it.name))
                    .append('\n')
              }

          it.srgToMethod.values()
              .each {
                builder.append('\t')
                // if the field is static use MS (Method Static) or M for just Method
                    .append(it.isStatic ? 'MS' : 'M')
                    .append(' ')
                    .append(firstNonNull(it.name, it.srgName))
                    .append(' ')
                    .append(firstNonNull(it.obfName, 'null'))
                    .append(' ')
                    .append(firstNonNull(it.srgName, it.name))
                    .append(' ')
                    .append(firstNonNull(it.descriptor, 'null'))
                    .append(' ')
                    .append(firstNonNull(it.obfDescriptor, 'null'))
                    .append('\n')
              }
        }

    output.write(builder.toString(), "utf-8")

    System.getProperties().setProperty('remapper.file', output.getAbsolutePath())
  }

  static final firstNonNull(Object a1, Object a2) {
    return a1 != null ? a1 : Objects.requireNonNull(a2);
  }
}
