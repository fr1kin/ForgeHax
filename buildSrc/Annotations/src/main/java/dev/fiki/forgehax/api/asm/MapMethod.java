package dev.fiki.forgehax.api.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MapMethod {
  Class<?> parentClass() default Dummy.class;
  MapClass parent() default @MapClass;

  String value() default "";
  String name() default "";

  Class<?>[] argTypes() default {Dummy.class};
  MapClass[] args() default {@MapClass};

  Class<?> retType() default Dummy.class;
  MapClass ret() default @MapClass;

  boolean allowInvalid() default false;
}
