package dev.fiki.forgehax.api.modloader.di.providers;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public abstract class AbstractDependencyProvider implements DependencyProvider {
  private final Class<?> targetClass;
  private final String qualifier;
}
