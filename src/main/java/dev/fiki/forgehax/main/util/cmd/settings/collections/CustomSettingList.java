package dev.fiki.forgehax.main.util.cmd.settings.collections;

import dev.fiki.forgehax.main.util.cmd.IParentCommand;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;
import lombok.Builder;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Supplier;

public class CustomSettingList<E extends IJsonSerializable>
    extends BaseCustomSettingCollection<E, List<E>> implements List<E> {
  @Builder
  public CustomSettingList(IParentCommand parent,
      String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      Supplier<List<E>> supplier,
      Supplier<E> valueSupplier) {
    super(parent, name, aliases, description, flags, supplier, valueSupplier);
  }

  @Override
  public boolean addAll(int i, Collection<? extends E> collection) {
    boolean ret = this.wrapping.addAll(collection);
    if(ret) {
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
    return wrapping.set(i, e);
  }

  @Override
  public void add(int i, E e) {
    wrapping.add(i, e);
    callUpdateListeners();
  }

  @Override
  public E remove(int i) {
    E ret = wrapping.remove(i);
    callUpdateListeners();
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
