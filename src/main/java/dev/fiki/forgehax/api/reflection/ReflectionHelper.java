package dev.fiki.forgehax.api.reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

public class ReflectionHelper {
  public static <H, T extends H> void copyOf(H host, T target,
      Class<? extends H> startClass,
      Class<? extends H> endClass,
      Predicate<Field> excludes)
    throws NoSuchFieldException, IllegalAccessException {
    Objects.requireNonNull(host);
    Objects.requireNonNull(target);
    
    for (Field field : startClass.getDeclaredFields()) {
      makePublic(field);

      if(isStatic(field) || excludes.test(field)) {
        continue;
      }

      if(isFinal(field)) {
        makeMutable(field);
      }
      
      field.set(target, field.get(host));
    }

    if(!startClass.equals(endClass) && startClass.getSuperclass() != null) {
      copyOf(host, target, startClass.getSuperclass(), endClass, excludes);
    }
  }
  
  public static <H, T extends H> void shallowCopyOf(H host, T target)
    throws NoSuchFieldException, IllegalAccessException {
    copyOf(host, target, host.getClass(), host.getClass(), ReflectionHelper::isFinal);
  }

  public static boolean isStatic(Member instance) {
    return (instance.getModifiers() & Modifier.STATIC) != 0;
  }

  public static boolean nonStatic(Member instance) {
    return !isStatic(instance);
  }
  
  public static boolean isFinal(Member instance) {
    return (instance.getModifiers() & Modifier.FINAL) != 0;
  }
  
  public static void makeAccessible(AccessibleObject instance, boolean accessible) {
    Objects.requireNonNull(instance);
    instance.setAccessible(accessible);
  }
  
  public static void makePublic(AccessibleObject instance) {
    makeAccessible(instance, true);
  }
  
  public static void makePrivate(AccessibleObject instance) {
    makeAccessible(instance, false);
  }
  
  public static void makeMutable(Member instance)
    throws NoSuchFieldException, IllegalAccessException {
    Objects.requireNonNull(instance);
    Field modifiers = Field.class.getDeclaredField("modifiers");
    makePublic(modifiers);
    modifiers.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
  }
  
  public static void makeImmutable(Member instance)
    throws NoSuchFieldException, IllegalAccessException {
    Objects.requireNonNull(instance);
    Field modifiers = Field.class.getDeclaredField("modifiers");
    makePublic(modifiers);
    modifiers.setInt(instance, instance.getModifiers() & Modifier.FINAL);
  }
}
