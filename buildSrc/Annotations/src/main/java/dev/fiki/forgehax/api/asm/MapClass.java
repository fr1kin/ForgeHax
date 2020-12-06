package dev.fiki.forgehax.api.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MapClass {
  Class<?> value() default Dummy.class;
  Class<?> classType() default Dummy.class;

  String className() default "";
  String[] innerClassName() default "";
  String[] innerClassNames() default {};
}
