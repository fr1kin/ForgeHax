package dev.fiki.forgehax.api.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMapping {
  Class<?> value() default void.class;
  String subClassName() default "";

  String _name() default "";
  String _obfName() default "";
}
