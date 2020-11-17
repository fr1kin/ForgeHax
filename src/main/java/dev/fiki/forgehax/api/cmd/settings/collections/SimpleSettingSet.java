package dev.fiki.forgehax.api.cmd.settings.collections;

import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.argument.IArgument;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class SimpleSettingSet<E> extends BaseSimpleSettingCollection<E, Set<E>> implements Set<E> {
  @Builder
  public SimpleSettingSet(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      Supplier<Set<E>> supplier, @Singular("defaultsTo") Collection<E> defaultsTo,
      IArgument<E> argument,
      @Singular List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags, supplier, defaultsTo, argument, listeners);
    onFullyConstructed();
  }
}
