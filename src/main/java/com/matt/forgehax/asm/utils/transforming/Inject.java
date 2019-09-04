package com.matt.forgehax.asm.utils.transforming;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created on 5/2/2017 by fr1kin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
  
  String description() default "empty";
  
  InjectPriority priority() default InjectPriority.DEFAULT;
}
