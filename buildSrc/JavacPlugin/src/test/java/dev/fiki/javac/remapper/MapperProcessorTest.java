package dev.fiki.javac.remapper;

import dev.fiki.forgehax.api.asm.runtime.RtMapClass;
import dev.fiki.forgehax.api.asm.runtime.RtMapField;
import dev.fiki.forgehax.api.asm.runtime.RtMapMethod;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapperProcessorTest extends CompilerTester {
  Output output;
  Class<?> testMapClass;

  @BeforeAll
  void setup() throws IOException {
    output = compile(getTemplatesDir(), "mapper.TestMapper");

    // write the classes out for viewing purposes
    for (CompilerOutput o : output.all()) {
      o.writeFile(getOutputDir());
    }

    testMapClass = output.find("mapper.TestMapper")
        .map(CompilerOutput::getClassInstance)
        .orElse(null);
  }

  @Test
  @DisplayName("Sanity check for test code logic. Make sure we have the correct class selected")
  void sanityCheckClassType() {
    assertThat(testMapClass)
        .isNotNull()
        .is(checkEquals("class name", Class::getSimpleName, "TestMapper"::equals));
  }

  @Test
  @DisplayName("Class is annotated with RtMapClass annotation")
  void annotationOnClassExists() {
    assertThat(testMapClass)
        .isNotNull()
        .hasAnnotation(RtMapClass.class);
  }

  @Test
  @DisplayName("Annotation RtMapField is on field0")
  void annotationOnFieldExists() {
    assertThatNoException()
        .isThrownBy(() -> {
          Field field = testMapClass.getField("field0");

          assertThat(field.isAnnotationPresent(RtMapField.class))
              .isTrue();
        });
  }

  @Test
  @DisplayName("Annotation RtMapMethod is on method0")
  void annotationOnMethod() {
    assertThatNoException()
        .isThrownBy(() -> {
          Method method = testMapClass.getMethod("method0", Object.class);

          assertThat(method.isAnnotationPresent(RtMapMethod.class))
              .isTrue();

          assertThat(method.getParameterCount())
              .isEqualTo(1);

          assertThat(method.getParameters()[0].isAnnotationPresent(RtMapClass.class))
              .isTrue();
        });
  }

  @Test
  @DisplayName("Annotation RtMapClass is on method0's first parameter")
  void annotationOnMethodParameter() {
    assertThatNoException()
        .isThrownBy(() -> {
          Method method = testMapClass.getMethod("method0", Object.class);

          assertThat(method.getParameterCount())
              .isEqualTo(1);

          assertThat(method.getParameters()[0].isAnnotationPresent(RtMapClass.class))
              .isTrue();
        });
  }
}
