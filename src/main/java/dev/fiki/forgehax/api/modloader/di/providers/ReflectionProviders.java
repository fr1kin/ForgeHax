package dev.fiki.forgehax.api.modloader.di.providers;

import com.google.common.collect.Maps;
import dev.fiki.forgehax.api.asm.runtime.RtMapClass;
import dev.fiki.forgehax.api.asm.runtime.RtMapField;
import dev.fiki.forgehax.api.asm.runtime.RtMapMethod;
import dev.fiki.forgehax.api.modloader.di.DependencyInjector;
import dev.fiki.forgehax.api.reflection.types.ReflectionClass;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;

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
      RtMapClass mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(RtMapClass.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any ClassMapping annotation: " + ctx));

      return NAME_TO_CLASS.computeIfAbsent(ASMClass.from(mapping), ReflectionClass::new);
    }
  }

  public static class FieldProvider extends AbstractDependencyProvider {
    public FieldProvider() {
      super(ReflectionField.class, null);
    }

    @Override
    public Object getInstance(BuildContext ctx, LoadChain chain) throws
        FailedToInitializeException, DependencyInjector.NoSuchDependency {
      RtMapField mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(RtMapField.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any FieldMapping annotation: " + ctx));

      return NAME_TO_FIELD.computeIfAbsent(ASMField.from(mapping), field -> new ReflectionField<>(
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
      RtMapMethod mapping = Stream.of(ctx.getContextParameter(), ctx.getContextField())
          .filter(Objects::nonNull)
          .map(e -> e.getAnnotation(RtMapMethod.class))
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> new FailedToInitializeException("Could not find any MethodMapping annotation: " + ctx));

      return NAME_TO_METHOD.computeIfAbsent(ASMMethod.from(mapping), method -> new ReflectionMethod<>(
          NAME_TO_CLASS.computeIfAbsent(method.getParentClass(), ReflectionClass::new),
          method
      ));
    }
  }
}
