package dev.fiki.forgehax.main.util.cmd.settings.collections;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public final class SimpleSettingSet<E> extends BaseSimpleSettingCollection<E, Set<E>> implements Set<E> {
  @Builder
  public SimpleSettingSet(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      Supplier<Set<E>> supplier, @Singular("defaultsTo") Collection<E> defaultsTo,
      IArgument<E> argument) {
    super(parent, name, aliases, description, flags, supplier, defaultsTo, argument);
    onFullyConstructed();
  }
}
