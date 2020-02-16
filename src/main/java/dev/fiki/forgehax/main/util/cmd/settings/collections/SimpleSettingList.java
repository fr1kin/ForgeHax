package dev.fiki.forgehax.main.util.cmd.settings.collections;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.argument.IArgument;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Supplier;

public final class SimpleSettingList<E> extends BaseSimpleSettingCollection<E, List<E>> implements List<E> {
  @Builder
  public SimpleSettingList(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      Supplier<List<E>> supplier, @Singular("defaultsTo") Collection<E> defaultTo,
      IArgument<E> argument) {
    super(parent, name, aliases, description, flags, supplier, defaultTo, argument);
    onFullyConstructed();
  }

  @Override
  public boolean addAll(int i, Collection<? extends E> collection) {
    return this.wrapping.addAll(collection);
  }

  @Override
  public E get(int i) {
    return wrapping.get(i);
  }

  @Override
  public E set(int i, E e) {
    return wrapping.set(i, e);
  }

  @Override
  public void add(int i, E e) {
    wrapping.add(i, e);
  }

  @Override
  public E remove(int i) {
    return wrapping.remove(i);
  }

  @Override
  public int indexOf(Object o) {
    return wrapping.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return wrapping.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return wrapping.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int i) {
    return wrapping.listIterator(i);
  }

  @Override
  public List<E> subList(int i, int i1) {
    return wrapping.subList(i, i1);
  }
}
