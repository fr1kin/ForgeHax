package dev.fiki.javac.remapper;

import dev.fiki.forgehax.api.event.ListenerList;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EventProcessor plugin")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventProcessorTest extends CompilerTester {
  Output output;

  @BeforeAll
  void setup() throws IOException {
    output = compile(getTemplatesDir(), "event.TestEvent");

    // write the classes out for viewing purposes
    for (CompilerOutput o : output.all()) {
      o.writeFile(getOutputDir());
    }
  }

  @Nested
  @DisplayName("TestEvent.class")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class TestEventTester {
    Class<?> testEventClass;

    @BeforeAll
    void setup() {
      testEventClass = output.find("event.TestEvent")
          .map(CompilerOutput::getClassInstance)
          .orElse(null);
    }

    @Test
    @DisplayName("sanity check on test")
    void sanityCheckClass() {
      assertThat(testEventClass)
          .isNotNull()
          .is(checkEquals("class name", Class::getSimpleName, "TestEvent"::equals));
    }

    @Test
    @DisplayName("default constructor exists")
    void checkIfHasDefaultConstructor() throws NoSuchMethodException {
      assertThat(testEventClass.getConstructor()).isNotNull();
    }

    @Test
    @DisplayName("call default constructor")
    void checkDefaultConstructorHasNoErrors() throws IllegalAccessException, InstantiationException {
      Object instance = testEventClass.newInstance();

      assertThat(instance).isNotNull();
      assertThat(instance).isInstanceOf(testEventClass);
    }

    @Test
    @DisplayName("generated LISTENER_LIST field")
    void hasListenerListField() throws NoSuchFieldException {
      Field field = testEventClass.getDeclaredField("LISTENER_LIST");

      assertThat(field.getModifiers() & Modifier.FINAL)
          .describedAs("is final")
          .isNotZero();

      assertThat(field.getModifiers() & Modifier.STATIC)
          .describedAs("is static")
          .isNotZero();

      assertThat(field.getType())
          .describedAs("is ListenerList type")
          .isEqualTo(ListenerList.class);
    }

    @Test
    @DisplayName("generated getListenerList method")
    void hasGetListenerListMethod() throws NoSuchMethodException {
      Method method = testEventClass.getDeclaredMethod("getListenerList");

      assertThat(method.getModifiers() & Modifier.STATIC)
          .describedAs("is not static")
          .isZero();

      assertThat(method.getReturnType())
          .describedAs("is ListenerList type")
          .isEqualTo(ListenerList.class);
    }

    @Test
    @DisplayName("generated listenerList method")
    void hasListenerListMethod() throws NoSuchMethodException {
      Method method = testEventClass.getDeclaredMethod("listenerList");

      assertThat(method.getModifiers() & Modifier.STATIC)
          .describedAs("is static")
          .isNotZero();

      assertThat(method.getReturnType())
          .describedAs("is ListenerList type")
          .isEqualTo(ListenerList.class);
    }

    @Test
    @DisplayName("generated eventCanceled field")
    void hasCancelableField() throws NoSuchFieldException {
      Field field = testEventClass.getDeclaredField("eventCanceled");

      assertThat(field.getModifiers() & Modifier.FINAL)
          .describedAs("is not final")
          .isZero();

      assertThat(field.getModifiers() & Modifier.STATIC)
          .describedAs("is not static")
          .isZero();

      assertThat(field.getType())
          .describedAs("is boolean return type")
          .isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("generated setCanceled method")
    void hasSetCanceled() throws NoSuchMethodException {
      Method method = testEventClass.getDeclaredMethod("setCanceled", boolean.class);

      assertThat(method.getModifiers() & Modifier.FINAL)
          .describedAs("is final")
          .isNotZero();

      assertThat(method.getModifiers() & Modifier.STATIC)
          .describedAs("is not static")
          .isZero();

      assertThat(method.getReturnType())
          .describedAs("is void return type")
          .isEqualTo(void.class);

      assertThat(method.getParameterCount())
          .describedAs("has 1 parameter")
          .isEqualTo(1);

      assertThat(method.getParameters()[0].getType())
          .describedAs("parameter is a boolean type")
          .isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("generated isCanceled method")
    void hasIsCanceled() throws NoSuchMethodException {
      Method method = testEventClass.getDeclaredMethod("isCanceled");

      assertThat(method.getModifiers() & Modifier.FINAL)
          .describedAs("is final")
          .isNotZero();

      assertThat(method.getModifiers() & Modifier.STATIC)
          .describedAs("is not static")
          .isZero();

      assertThat(method.getReturnType())
          .describedAs("is boolean return type")
          .isEqualTo(boolean.class);
    }

    @Nested
    @DisplayName("new instance")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ListenerListTests {
      Object instance;

      @BeforeAll
      void setup() throws IllegalAccessException, InstantiationException {
        instance = testEventClass.newInstance();
      }

      @Test
      @DisplayName("Implicit calls to TestEvent.listenerList() should call the injected static method")
      void checkImplicitStaticListenerListResult() throws NoSuchMethodException,
          InvocationTargetException, IllegalAccessException {
        Method method = testEventClass.getMethod("callImplicitListenerList");
        Object ret = method.invoke(instance);

        assertThat(ret).isNotNull();
        assertThat(ret).isInstanceOf(ListenerList.class);
      }

      @Test
      @DisplayName("Explicit calls to TestEvent.listenerList() should call the injected static method")
      void checkExplicitStaticListenerListResult() throws NoSuchMethodException,
          InvocationTargetException, IllegalAccessException {
        Method method = testEventClass.getMethod("callExplicitListenerList");
        Object ret = method.invoke(instance);

        assertThat(ret).isNotNull();
        assertThat(ret).isInstanceOf(ListenerList.class);
      }

      @Test
      @DisplayName("Sanity check to prove calling Event.listenerList() throws an exception")
      void sanityCheckListenerList() throws NoSuchMethodException {
        Method method = testEventClass.getMethod("callParentListenerList");

        assertThatThrownBy(() -> method.invoke(instance))
            .extracting(Throwable::getCause)
            .isInstanceOf(UnsupportedOperationException.class);
      }
    }
  }
}
