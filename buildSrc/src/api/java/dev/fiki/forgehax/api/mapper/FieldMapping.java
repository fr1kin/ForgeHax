package dev.fiki.forgehax.api.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldMapping {
  Class<?> parentClass() default void.class;

  String value();

  MappedFormat format() default MappedFormat.MAPPED;

  String _name() default "";
  String _obfName() default "";
  String _srgName() default "";
}
