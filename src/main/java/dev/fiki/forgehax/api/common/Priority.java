package dev.fiki.forgehax.api.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 6/27/2017 by fr1kin
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
  
  PriorityEnum value() default PriorityEnum.DEFAULT;
}
