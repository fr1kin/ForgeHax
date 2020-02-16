package dev.fiki.forgehax.main.util.cmd.settings.collections;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;
import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

public final class CustomSettingSet<E extends IJsonSerializable> extends BaseCustomSettingCollection<E, Set<E>> {
  @Builder
  public CustomSettingSet(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      Supplier<Set<E>> supplier, @Singular("defaultsTo") Collection<E> defaultTo,
      Supplier<E> valueSupplier) {
    super(parent, name, aliases, description, flags, supplier, defaultTo, valueSupplier);
    onFullyConstructed();
  }
}
