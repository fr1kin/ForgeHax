package dev.fiki.javac.remapper;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import lombok.javac.apt.LombokProcessor;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Condition;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CompilerTester {
  private static Path templateDir;
  private static Path outputDir;

  public static <E, R> Condition<E> checkEquals(String desc,
      Function<E, R> extractor,
      Predicate<R> predicate) {
    return new Condition<>(o -> predicate.test(extractor.apply(o)), desc);
  }

  public Path getTemplatesDir() {
    if (templateDir == null) {
      String dir = Objects.requireNonNull(System.getenv("TEMPLATES_DIR"),
          "Missing TEMPLATES_DIR environment variable!");
      templateDir = Paths.get(dir);
    }
    return templateDir;
  }

  public Path getOutputDir() {
    if (outputDir == null) {
      String dir = Objects.requireNonNull(System.getenv("BUILD_DIR"),
          "Missing BUILD_DIR environment variable!");
      outputDir = Paths.get(dir).resolve("test-class-output");

      try {
        // delete all the files currently in this directory (if it exists)
//        recursivelyDelete(outputDir, 0);
        if (!Files.exists(outputDir)) {
          Files.createDirectory(outputDir);
        }
      } catch (IOException e) {
        throw new Error(e);
      }
    }
    return outputDir;
  }

  private static void recursivelyDelete(Path file, int depth) throws IOException {
    if (depth < 3) {
      if (Files.isDirectory(file)) {
        for (Path child : Files.list(file).collect(Collectors.toList())) {
          recursivelyDelete(child, depth + 1);
        }
      } else {
        Files.deleteIfExists(file);
      }
    }
  }

  public Output compile(Path root, String qualifiedName) throws IOException {
    Path src = root.resolve(qualifiedName.replace('.', '/') + ".java");

    if (!Files.exists(src)) {
      throw new Error("File for class not found: " + qualifiedName);
    }

    Compilation compilation = Compiler.javac()
        .withProcessors(new LombokProcessor())
        .withOptions(Arrays.asList("-classpath", System.getProperty("java.class.path"), "-g:none"))
        .compile(new SourceFile(src));

    return new Output(compilation.generatedFiles());
  }

  static class SourceFile extends SimpleJavaFileObject {
    private final Path file;

    public SourceFile(Path file) {
      super(file.toUri(), Kind.SOURCE);
      this.file = file;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      return new String(Files.readAllBytes(file));
    }
  }

  public static class Output {
    private final List<CompilerOutput> outputs;

    public Output(List<JavaFileObject> objects) {
      this.outputs = objects.stream()
          .map(CompilerOutput::new)
          .collect(Collectors.toList());
    }

    public List<CompilerOutput> all() {
      return outputs;
    }

    public Optional<CompilerOutput> find(String contains) {
      return outputs.stream()
          .filter(co -> co.getCanonicalName().contains(contains))
          .findAny();
    }
  }

  public static class CompilerOutput {
    private final JavaFileObject jfo;

    CompilerOutput(JavaFileObject jfo) {
      this.jfo = jfo;
    }

    public byte[] getFileBytes() throws IOException {
      return IOUtils.toByteArray(jfo.openInputStream());
    }

    public String getFileName() {
      String name = jfo.getName().replace(File.pathSeparatorChar, '/');
      if (name.startsWith("/CLASS_OUTPUT/")) {
        return name.substring("/CLASS_OUTPUT/".length());
      } else {
        return name;
      }
    }

    public String getCanonicalName() {
      String fn = getFileName();
      return fn.substring(0, fn.lastIndexOf(".class")).replace('/', '.');
    }

    public void writeFile(Path dir) throws IOException {
      String name = getFileName();

      Path file = dir.resolve(name);
      if (!Files.exists(file.getParent())) {
        Files.createDirectories(file.getParent());
      }

      Files.write(file, getFileBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public ClassLoader getClassLoader() {
      return new ClassLoader() {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
          byte[] bytes;
          try {
            bytes = getFileBytes();
          } catch (IOException e) {
            throw new Error(e);
          }

          return defineClass(name, bytes, 0, bytes.length);
        }
      };
    }

    public Class<?> getClassInstance() {
      try {
        return getClassLoader().loadClass(getCanonicalName());
      } catch (ClassNotFoundException e) {
        throw new Error(e);
      }
    }
  }
}
