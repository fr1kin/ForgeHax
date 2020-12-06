package dev.fiki.forgehax.api.asm.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RtMapMethod {
  RtMapClass parent();
  String name();
  String descriptor();
  Alternative[] alternatives() default {};

  @interface Alternative {
    String name();
    String descriptor();
    Format format();
  }
}
