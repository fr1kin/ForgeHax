package dev.fiki.forgehax.api.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodMapping {
  /**
   * CODEGEN ONLY
   * <p>
   * The parent class of the mapping this object represents.
   * If not changed, the code generator will use the class provided by the parent classes {@code @ClassMapping}
   *
   * @return parent class
   */
  Class<?> parentClass() default void.class;

  /**
   * CODEGEN ONLY
   * <p>
   * The name of the method to attempt to lookup mappings for.
   *
   * @return method name
   */
  String value();

  /**
   * CODEGEN ONLY
   * <p>
   * The arguments for the method. This can be omitted if the {@code value} and/or {@code ret} is unique.
   *
   * @return Array of argument types
   */
  Class<?>[] args() default {};

  /**
   * CODEGEN ONLY
   * <p>
   * The return type of the method. This can be omitted if the {@code value} and/or {@code args} is unique.
   *
   * @return Return type
   */
  Class<?> ret() default void.class;

  /**
   * To help the code generator know if the method is static or not.
   *
   * @return if the method is static or not
   */
  boolean isStatic() default false;

  /**
   * If {@code value} is not a mcp mapping name, the type can be changed to srg or obfuscated.
   *
   * @return the type of mapping name provided
   */
  MappedFormat format() default MappedFormat.MAPPED;

  String _name() default "";
  String _obfName() default "";
  String _srgName() default "";

  String _descriptor() default "";
  String _obfDescriptor() default "";
}
