package mapper;

import dev.fiki.forgehax.api.asm.MapClass;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;

import java.util.Objects;

class SampleClass {
  private Integer aField;

  public int foo(SampleClass.InnerClass arg0, Integer b, Double c, float[] d) {
    return 0;
  }

  public class InnerClass {
    private Object anotherField;
  }

  public class SubClass {
    public class DoubleSubClass {
    }
  }
}

@MapClass(SampleClass.class)
public class TestMapper {
  @MapField(parentClass = SampleClass.InnerClass.class, name = "anotherField")
  public Object field0;

  @MapMethod(value = "foo", argTypes = {SampleClass.InnerClass.class, Integer.class, Double.class, float[].class})
  public Object method0(@MapClass(classType = Objects.class) Object param0) {
    return null;
  }
}
