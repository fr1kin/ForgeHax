package dev.fiki.javac.remapper.template;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;

import java.util.Objects;

@MapClass(SampleClass.class)
public class Test {
  @MapField(parentClass = SampleClass.InnerClass.class, name = "anotherField")
  public Object field0;

  @MapMethod(value = "foo", argTypes = {SampleClass.InnerClass.class, Integer.class, Double.class, float.class})
  public Object method0(@MapClass(classType = Objects.class) Object param0) {
    return null;
  }
}
