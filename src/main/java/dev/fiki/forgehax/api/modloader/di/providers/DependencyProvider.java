package dev.fiki.forgehax.api.modloader.di.providers;

import dev.fiki.forgehax.api.modloader.di.DependencyInjector;
import lombok.*;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Stack;
import java.util.stream.Collectors;

public interface DependencyProvider {
  Class<?> getTargetClass();

  @Nullable
  default String getQualifier() {
    return null;
  }

  default boolean shouldInitialize() {
    return false;
  }

  Object getInstance(BuildContext ctx, LoadChain chain) throws
      FailedToInitializeException, DependencyInjector.NoSuchDependency;

  default Object getInstance(DependencyInjector injector) throws
      FailedToInitializeException, DependencyInjector.NoSuchDependency {
    return getInstance(noBuildContext(), new LoadChain(injector));
  }

  static String getElementQualifier(AnnotatedElement element) {
    return DependencyInjector.extractQualifier(element);
  }

  static BuildContext noBuildContext() {
    return BuildContext.builder().build();
  }

  static LoadChain createLoadChain(DependencyInjector manager) {
    return new LoadChain(manager);
  }

  @Getter
  @Builder
  @AllArgsConstructor
  @ToString
  class BuildContext {
    @Nullable
    private final String qualifier;

    @Nullable
    private final Class<?> contextClass;

    @Nullable
    private final Parameter contextParameter;

    @Nullable
    private final Field contextField;
  }

  @Getter
  @RequiredArgsConstructor
  class LoadChain {
    private final DependencyInjector injector;
    private final Stack<DependencyProvider> stack = new Stack<>();

    public Object getOrCreate(BuildContext ctx, Class<?> target)
        throws FailedToInitializeException, DependencyInjector.NoSuchDependency {
      DependencyProvider dep = injector.getDependency(target, ctx.getQualifier());

      if (stack.contains(dep)) {
        throw new Error("Circular dependency injection! Load stack: " +
            stack.stream()
                .map(DependencyProvider::getTargetClass)
                .map(Class::getName)
                .collect(Collectors.joining(" -> ")));
      }

      stack.push(dep);

      try {
        return dep.getInstance(ctx, this);
      } catch(Throwable t) {
        throw new FailedToInitializeException("Failed to get instance of class: " + target.getName(), t);
      } finally {
        stack.pop();
      }
    }
  }

  class FailedToInitializeException extends Exception {
    public FailedToInitializeException(String message, Throwable cause) {
      super(message, cause);
    }

    public FailedToInitializeException() {
      super();
    }

    public FailedToInitializeException(String message) {
      super(message);
    }

    public FailedToInitializeException(Throwable cause) {
      super(cause);
    }
  }
}
