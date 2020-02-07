package dev.fiki.forgehax.main.util.cmd.settings.collections;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;
import lombok.Builder;

import java.util.Set;
import java.util.function.Supplier;

public class CustomSettingSet<E extends IJsonSerializable> extends BaseCustomSettingCollection<E, Set<E>> {
  @Builder
  public CustomSettingSet(IParentCommand parent,
      String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      Supplier<Set<E>> supplier,
      Supplier<E> valueSupplier) {
    super(parent, name, aliases, description, flags, supplier, valueSupplier);
  }
}
