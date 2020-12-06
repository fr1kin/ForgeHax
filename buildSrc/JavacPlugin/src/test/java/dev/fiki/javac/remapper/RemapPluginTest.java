package dev.fiki.javac.remapper;

import dev.fiki.javac.remapper.test.TestCompiler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RemapPluginTest {
  private static final String TEMPLATE = "package dev.fiki.javac.remapper.template;\n" +
      "\n" +
      "import dev.fiki.forgehax.api.asm.MapClass;\n" +
      "import dev.fiki.forgehax.api.asm.MapField;\n" +
      "import dev.fiki.forgehax.api.asm.MapMethod;\n" +
      "\n" +
      "import java.util.Objects;\n" +
      "\n" +
      "@MapClass(SampleClass.class)\n" +
      "public class Test {\n" +
      "  @MapField(parentClass = SampleClass.InnerClass.class, name = \"anotherField\")\n" +
      "  public Object field0;\n" +
      "\n" +
      "  @MapMethod(value = \"foo\", argTypes = {SampleClass.InnerClass.class, Integer.class, Double.class, float[].class})\n" +
      "  public Object method0(@MapClass(classType = Objects.class) Object param0) {\n" +
      "    return null;\n" +
      "  }\n" +
      "}\n";

  private byte[] classBytes;

  @BeforeAll
  public void setup() throws IOException {
    TestCompiler compiler = new TestCompiler();
    classBytes = compiler.compile("dev.fiki.javac.remapper.template", "TestTemplate",
        TEMPLATE.replace("class Test {", "class TestTemplate {"));
  }

  @Test
  public void test() {
    ClassReader cr = new ClassReader(classBytes);
    PrintWriter pw = new PrintWriter(System.out);
    TraceClassVisitor cv = new TraceClassVisitor(pw);
    cr.accept(cv, 0);
  }
}
