package dev.fiki.forgehax.api.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MapField {
  Class<?> parentClass() default Dummy.class;
  MapClass parent() default @MapClass;

  String value() default "";
  String name() default "";

  Class<?> typeClass() default Dummy.class;
  MapClass type() default @MapClass;

  boolean allowInvalid() default false;
}
