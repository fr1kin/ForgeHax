package dev.fiki.javac.remapper.template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface TestAnnotation {
  String arg0() default "";
  int arg1() default 0;
  Class<?> arg2() default Object.class;
  Class<?>[] arg3() default {};
  ParentAnnotation arg4() default @ParentAnnotation(arg0 = Integer.class);
  ParentAnnotation[] arg5() default {@ParentAnnotation(arg0 = Integer.class), @ParentAnnotation};
}
