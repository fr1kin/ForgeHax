package dev.fiki.forgehax.api.cmd.settings.collections;

import dev.fiki.forgehax.api.cmd.IParentCommand;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import dev.fiki.forgehax.api.serialization.IJsonSerializable;
import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Supplier;

public final class CustomSettingList<E extends IJsonSerializable>
    extends BaseCustomSettingCollection<E, List<E>> implements List<E> {
  @Builder
  public CustomSettingList(IParentCommand parent,
      String name, @Singular Set<String> aliases, String description,
      @Singular Set<EnumFlag> flags,
      Supplier<List<E>> supplier, @Singular("defaultsTo") Collection<E> defaultTo,
      Supplier<E> valueSupplier,
      @Singular List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags, supplier, defaultTo, valueSupplier, listeners);
    onFullyConstructed();
  }

  @Override
  public boolean addAll(int i, Collection<? extends E> collection) {
    boolean ret = this.wrapping.addAll(collection);
    if (ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public E get(int i) {
    return wrapping.get(i);
  }

  @Override
  public E set(int i, E e) {
    E v = wrapping.set(i, e);
    if (v != null) {
      callUpdateListeners();
    }
    return v;
  }

  @Override
  public void add(int i, E e) {
    int beforeSize = size();
    wrapping.add(i, e);
    if (size() != beforeSize) {
      callUpdateListeners();
    }
  }

  @Override
  public E remove(int i) {
    E ret = wrapping.remove(i);
    if (ret != null) {
      callUpdateListeners();
    }
    return ret;
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
