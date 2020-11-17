package dev.fiki.forgehax.api.modloader.di.providers;

import dev.fiki.forgehax.api.modloader.di.DependencyInjector;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class SingletonDependency extends AbstractDependencyProvider {
  private Object instance = null;

  public SingletonDependency(Class<?> targetClass, @Nullable String qualifier) {
    super(targetClass, qualifier);
  }

  @Override
  public boolean shouldInitialize() {
    return true;
  }

  @Override
  public Object getInstance(BuildContext ctx, LoadChain chain) throws
      FailedToInitializeException, DependencyInjector.NoSuchDependency {
    // if already initialized, get that object
    if (instance != null) {
      return instance;
    }

    Constructor<?>[] constructors = getTargetClass().getConstructors();

    if (constructors.length <= 0) {
      throw new FailedToInitializeException("Could not find any public constructor for class " + getTargetClass().getName());
    } else if (constructors.length > 1) {
      throw new FailedToInitializeException("Too many constructors found for class " + getTargetClass().getName()
          + " (expected 1, got " + constructors.length + ")");
    }

    // get the only constructor that should be in this list
    Constructor<?> constructor = constructors[0];

    Parameter[] parameters = constructor.getParameters();
    Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      Class<?> type = parameter.getType();

      String qualifier = DependencyProvider.getElementQualifier(parameter);
      Field field = null;

      try {
        field = getTargetClass().getDeclaredField(parameter.getName());

        // check field type
        if (!type.isAssignableFrom(field.getType())) {
          field = null; // discard
        }
      } catch (Throwable t) {
        // field is optional
      }

      // try and extract qualifier from the field that matches the parameter
      if (field != null && qualifier == null) {
        qualifier = DependencyProvider.getElementQualifier(field);
      }

      args[i] = chain.getOrCreate(
          BuildContext.builder()
              .qualifier(qualifier)
              .contextParameter(parameter)
              .contextField(field)
              .build(),
          type);
    }

    try {
      return instance = constructor.newInstance(args);
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
      throw new FailedToInitializeException(e);
    }
  }
}
