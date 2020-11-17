package dev.fiki.forgehax.api.modloader.di;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import dev.fiki.forgehax.api.modloader.di.providers.AlreadyInitializedDependency;
import dev.fiki.forgehax.api.modloader.di.providers.DependencyProvider;
import dev.fiki.forgehax.api.modloader.di.providers.SingletonDependency;
import lombok.NonNull;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyInjector {
  private final Set<DependencyProvider> dependencies = Sets.newTreeSet(
      Comparator.comparing(DependencyProvider::getTargetClass, Comparator.comparing(Class::getName))
          .thenComparing(DependencyProvider::getQualifier, Comparator.comparing(Strings::nullToEmpty, String::compareTo)));

  public void provider(DependencyProvider provider) {
    if (!exists(provider.getTargetClass(), provider.getQualifier())) {
      dependencies.add(provider);
    }
  }

  @SneakyThrows
  public void module(Class<?> clazz, String qualifier) {
    if (!exists(clazz, qualifier)) {
      DependencyProvider dp = new SingletonDependency(clazz, qualifier);
      dependencies.add(dp);
    } else {
      throw new Error("Failed to add dependency because a similar one already exists");
    }
  }

  public void module(Class<?> clazz) {
    module(clazz, extractQualifier(clazz));
  }

  public void addInstance(@NonNull Object instance, Class<?> clazz, String qualifier) {
    if (!exists(clazz, qualifier)) {
      dependencies.add(new AlreadyInitializedDependency(clazz, qualifier, instance));
    } else {
      throw new Error("Failed to add dependency because a similar one already exists");
    }
  }

  public void addInstance(@NonNull Object instance, String qualifier) {
    addInstance(instance, instance.getClass(), qualifier);
  }

  public void addInstance(@NonNull Object instance, Class<?> clazz) {
    addInstance(instance, clazz, extractQualifier(clazz));
  }

  public void addInstance(@NonNull Object instance) {
    addInstance(instance, extractQualifier(instance.getClass()));
  }

  public DependencyProvider getDependency(Class<?> clazz, @Nullable String qualifier) throws NoSuchDependency {
    List<DependencyProvider> matches = dependencies.stream()
        .filter(dp -> clazz.equals(dp.getTargetClass()))
        .filter(dp -> qualifier == null || qualifier.equals(dp.getQualifier()))
        .collect(Collectors.toList());

    // only possible match, it must be this provider
    if (matches.size() == 1) {
      return matches.get(0);
    } else {
      // if no matches, we will try and find the best possible match
      if (matches.isEmpty()) {
        matches = dependencies.stream()
            // instead of a direct class match, we will look for classes that extend clazz
            .filter(dp -> clazz.isAssignableFrom(dp.getTargetClass()))
            .filter(dp -> qualifier == null || qualifier.equals(dp.getQualifier()))
            .collect(Collectors.toList());
      }

      if (matches.isEmpty()) {
        throw new NoSuchDependency("No dependency with qualifier \"" + qualifier
            + "\" exists for class " + clazz.getName());
      } else if (matches.size() > 1) {
        throw new NoSuchDependency("Dependency " + clazz.getName()
            + " has multiple matches for qualifier (" + qualifier + ")!");
      } else {
        return matches.get(0);
      }
    }
  }

  public boolean exists(Class<?> clazz, @Nullable String qualifier) {
    try {
      return getDependency(clazz, qualifier) != null;
    } catch (NoSuchDependency e) {
      return false;
    }
  }

  public DependencyProvider getDependency(Class<?> clazz) throws NoSuchDependency {
    return getDependency(clazz, null);
  }

  public Stream<DependencyProvider> getDependenciesAnnotatedWith(final Class<? extends Annotation> annotationClass) {
    return dependencies.stream()
        .filter(dep -> dep.getTargetClass().isAnnotationPresent(annotationClass));
  }

  @SneakyThrows
  public <T> T getInstance(Class<T> clazz, String qualifier) {
    return (T) getSingletonInstance(getDependency(clazz, qualifier));
  }

  public <T> T getInstance(Class<T> clazz) {
    return getInstance(clazz, null);
  }

  public <T> Stream<T> getInstances(Class<T> baseClass) {
    return dependencies.stream()
        .filter(dp -> baseClass.isAssignableFrom(dp.getTargetClass()))
        .map(this::getSingletonInstance)
        .map(baseClass::cast);
  }

  public static String extractQualifier(AnnotatedElement element) {
    if (element.isAnnotationPresent(Injected.class)) {
      return Strings.emptyToNull(element.getAnnotation(Injected.class).value());
    }
    return null;
  }

  @SneakyThrows
  private Object getSingletonInstance(DependencyProvider provider) {
    return provider.getInstance(this);
  }

  public static class NoSuchDependency extends Exception {
    public NoSuchDependency(String message) {
      super(message);
    }
  }
}
