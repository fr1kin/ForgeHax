package dev.fiki.forgehax.api.event;

import dev.fiki.forgehax.api.common.PriorityEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeListener {
  PriorityEnum priority() default PriorityEnum.DEFAULT;
  int flags() default ListenerFlags.NONE;
}
