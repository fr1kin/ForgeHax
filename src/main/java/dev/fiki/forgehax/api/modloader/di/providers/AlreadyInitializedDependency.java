package dev.fiki.forgehax.api.modloader.di.providers;

import dev.fiki.forgehax.api.modloader.di.DependencyInjector;
import lombok.NonNull;

public class AlreadyInitializedDependency extends AbstractDependencyProvider {
  private final Object instance;

  public AlreadyInitializedDependency(Class<?> targetClass, String qualifier, @NonNull Object instance) {
    super(targetClass, qualifier);
    this.instance = instance;
  }

  @Override
  public Object getInstance(BuildContext ctx, LoadChain chain) throws
      FailedToInitializeException, DependencyInjector.NoSuchDependency {
    return instance;
  }
}
