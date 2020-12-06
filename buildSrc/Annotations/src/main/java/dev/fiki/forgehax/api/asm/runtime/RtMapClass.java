package dev.fiki.forgehax.api.asm.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RtMapClass {
  String className();
  Alternative[] alternatives() default {};

  @interface Alternative {
    String className();
    Format format();
  }
}
