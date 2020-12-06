package dev.fiki.javac.remapper.template;

public class SampleClass {
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
