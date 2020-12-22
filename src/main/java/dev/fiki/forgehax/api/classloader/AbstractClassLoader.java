package dev.fiki.forgehax.api.classloader;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.getLauncherClassLoader;

/**
 * Created on 2/13/2018 by fr1kin
 */
@Log4j2
public abstract class AbstractClassLoader<E> {
  
  protected AbstractClassLoader() {
  }
  
  /**
   * The class that must be extended
   *
   * @return class
   */
  @Nullable
  public abstract Class<E> getInheritedClass();
  
  /**
   * The optional annotation class that must be on top of every class
   *
   * @return null if no annotation is required
   */
  @Nullable
  public abstract Class<? extends Annotation> getAnnotationClass();
  
  /**
   * Gets all the classes in the package that extend 'extendsClass' and are annotated with
   * 'annotationClass'
   *
   * @param classLoader class loader to use
   * @param classPaths class paths to initialize
   * @return collection of classes that match the required conditions
   * @throws IOException if package has trouble being read
   */
  @SuppressWarnings("unchecked")
  public Collection<Class<? extends E>> filterClassPaths(
      ClassLoader classLoader, Collection<Path> classPaths) throws IOException {
    return ClassLoaderHelper.getLoadedClasses(classLoader, classPaths)
        .stream()
        .filter(this::checkAnnotation)
        .filter(this::checkInheritedClass)
        .map(this::wildCast)
        .collect(Collectors.toList());
  }
  
  /**
   * Initializes all the classes from ::create and returns a list of non-null instances created from
   * the provided classes
   */
  public Collection<? extends E> loadClasses(Collection<Class<? extends E>> classes) {
    return classes.stream().map(this::create).filter(Objects::nonNull).collect(Collectors.toList());
  }
  
  public E loadClass(Class<? extends E> clazz) {
    return loadClasses(Collections.singleton(clazz)).stream().findFirst().orElse(null);
  }
  
  protected E create(Class<? extends E> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      log.error("Failed to create new instance of {}", clazz.getSimpleName());
      log.error(e, e);
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  private Class<? extends E> wildCast(Class<?> clazz) {
    return (Class<? extends E>) clazz;
  }
  
  private boolean checkAnnotation(Class<?> clazz) {
    return getAnnotationClass() == null || clazz.isAnnotationPresent(getAnnotationClass());
  }
  
  private boolean checkInheritedClass(Class<?> clazz) {
    return getInheritedClass() == null || getInheritedClass().isAssignableFrom(clazz);
  }
  
  //
  //
  //
  
  public static ClassLoader getFMLClassLoader() {
    return getLauncherClassLoader();
  }
}
