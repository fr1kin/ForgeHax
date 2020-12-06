package dev.fiki.javac.remapper.test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import lombok.javac.apt.LombokProcessor;
import org.apache.commons.io.IOUtils;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Arrays;

public class TestCompiler {
  public byte[] compile(String packageName, String className, String testSource) throws IOException {
    Compilation compilation = Compiler.javac()
        .withProcessors(new LombokProcessor())
        .withOptions(Arrays.asList("-classpath", System.getProperty("java.class.path"), "-g:none"))
        .compile(new SimpleSourceFile(packageName + "." + className, testSource));

    JavaFileObject file = compilation
        .generatedFile(StandardLocation.CLASS_OUTPUT, packageName, className + ".class")
        .orElseThrow(() -> new Error("Could not find class " + className + ".class"));

    return IOUtils.toByteArray(file.openInputStream());
  }
}
