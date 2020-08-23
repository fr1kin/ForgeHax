package dev.fiki.forgehax.main.util.modloader.di.providers;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import dev.fiki.forgehax.main.util.modloader.di.DependencyInjector;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionClass;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionMethod;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ReflectionProviders {
  private static final Map<ASMClass, ReflectionClass<?>> NAME_TO_CLASS = Maps.newHashMap();
  private static final Map<ASMField, ReflectionField<?>> NAME_TO_FIELD = Maps.newHashMap();
  private static final Map<ASMMethod, ReflectionMethod<?>> NAME_TO_METHOD = Maps.newHashMap();

  public static void all(DependencyInjector injector) {
    injector.provider(new ClassProvider());
    injector.provider(new FieldProvider());
    injector.provider(new MethodProvider());
  }

  public static class ClassProvider extends AbstractDependencyProvider {
    public ClassProvider() {
      super(ReflectionClass.class, null);
    }

    @Override
    public Object getInstance(BuildContext ctx, LoadChain chain) throws
        FailedToInitializeException, DependencyInjector.NoSuchDependency {
      ClassMapping mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(ClassMapping.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any ClassMapping annotation: " + ctx));

      return NAME_TO_CLASS.computeIfAbsent(ASMClass.unmap(mapping), ReflectionClass::new);
    }
  }

  public static class FieldProvider extends AbstractDependencyProvider {
    public FieldProvider() {
      super(ReflectionField.class, null);
    }

    @Override
    public Object getInstance(BuildContext ctx, LoadChain chain) throws
        FailedToInitializeException, DependencyInjector.NoSuchDependency {
      FieldMapping mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(FieldMapping.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any FieldMapping annotation: " + ctx));

      return NAME_TO_FIELD.computeIfAbsent(ASMField.unmap(mapping), field -> new ReflectionField<>(
          NAME_TO_CLASS.computeIfAbsent(field.getParentClass(), ReflectionClass::new),
          field
      ));
    }
  }

  public static class MethodProvider extends AbstractDependencyProvider {
    public MethodProvider() {
      super(ReflectionMethod.class, null);
    }

    @Override
    public Object getInstance(BuildContext ctx, LoadChain chain) throws
        FailedToInitializeException, DependencyInjector.NoSuchDependency {
      MethodMapping mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(MethodMapping.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any MethodMapping annotation: " + ctx));

      return NAME_TO_METHOD.computeIfAbsent(ASMMethod.unmap(mapping), method -> new ReflectionMethod<>(
          NAME_TO_CLASS.computeIfAbsent(method.getParentClass(), ReflectionClass::new),
          method
      ));
    }
  }
}
