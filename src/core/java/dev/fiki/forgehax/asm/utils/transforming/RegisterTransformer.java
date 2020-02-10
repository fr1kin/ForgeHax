package dev.fiki.forgehax.asm.utils.transforming;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterTransformer {

  // If the transformer class that this transformer extends or implements does not have parameterized types for the node type
  Class<?> nodeType() default void.class;
  // description
  String value() default "empty";

  // If this is true and the transformer fails we (((shut it down)))
  boolean required() default false;
}
