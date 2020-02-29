package dev.fiki.forgehax.main.util.cmd;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractSettingCollection<E, L extends Collection<E>>
    extends AbstractParentCommand implements ISettingCollection<E, L> {
  @Getter(AccessLevel.PROTECTED)
  protected final L wrapping;

  public AbstractSettingCollection(IParentCommand parent,
      String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      @NonNull Supplier<L> supplier, @NonNull Collection<E> defaultTo) {
    super(parent, name, aliases, description, flags);
    this.wrapping = supplier.get();
    this.wrapping.addAll(defaultTo);
  }

  @Override
  public Optional<E> search(Predicate<E> predicate) {
    for(E obj : this) {
      if(obj != null && predicate.test(obj)) {
        return Optional.of(obj);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<E> get(E instance) {
    return search(instance::equals);
  }

  @Override
  public int size() {
    return wrapping.size();
  }

  @Override
  public boolean isEmpty() {
    return wrapping.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return wrapping.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return wrapping.iterator();
  }

  @Override
  public Object[] toArray() {
    return wrapping.toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return wrapping.toArray(ts);
  }

  @Override
  public boolean add(E e) {
    boolean ret = wrapping.add(e);
    if(ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public boolean remove(Object o) {
    boolean ret = wrapping.remove(o);
    if(ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return this.wrapping.containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    boolean ret = this.wrapping.addAll(collection);
    if(ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean ret = this.wrapping.removeAll(collection);
    if(ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    boolean ret = this.wrapping.retainAll(collection);
    if(ret) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public void clear() {
    wrapping.clear();
    callUpdateListeners();
  }

  @Override
  public String toString() {
    return getName() +
        " = [" +
        wrapping.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", ")) +
        "]";
  }
}
