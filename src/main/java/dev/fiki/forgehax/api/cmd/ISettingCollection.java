package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.serialization.IJsonSerializable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface ISettingCollection<E, L extends Collection<E>> extends IParentCommand, IJsonSerializable, Collection<E> {
  Optional<E> search(Predicate<E> predicate);
  Optional<E> get(E instance);
}
