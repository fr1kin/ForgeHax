package dev.fiki.forgehax.mapper.extractor

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

class StaticMethodsImporter implements Importer {
  final File file

  StaticMethodsImporter(File file) {
    this.file = file
  }

  @Override
  void read(MapData data) {
    file.newReader().withCloseable {
      final scanner = new Scanner(it)
      while (scanner.hasNextLine()) {
        data.srgToMethod.get(scanner.nextLine())?.isStatic = true
      }
    }
  }

  static StaticMethodsImporter fromMcpConfigZip(File configZip) {
    def zip = new ZipFile(Objects.requireNonNull(configZip, 'no config zip provided'))
    return zip.entries().with {
      while (it.hasMoreElements()) {
        def e = it.nextElement()
        if ('config/static_methods.txt' == e.getName()) {
          def tmp = File.createTempFile("temp", null)
          tmp.deleteOnExit()

          zip.getInputStream(e).withCloseable { Files.copy(it, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING) }

          return new StaticMethodsImporter(tmp)
        }
      }
      throw new Error('Cannot find static method file!')
    }
  }
}
