package dev.fiki.forgehax.api.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractSettingCollection<E, L extends Collection<E>>
    extends AbstractParentCommand implements ISettingCollection<E, L> {
  private final Multimap<Class<? extends ICommandListener>, ICommandListener> listeners =
      Multimaps.newListMultimap(Maps.newConcurrentMap(), Lists::newCopyOnWriteArrayList);

  @Getter(AccessLevel.PROTECTED)
  protected final L wrapping;

  public AbstractSettingCollection(IParentCommand parent,
      String name, Set<String> aliases, String description,
      Set<EnumFlag> flags,
      @NonNull Supplier<L> supplier, @NonNull Collection<E> defaultTo,
      List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags);
    this.wrapping = supplier.get();
    this.wrapping.addAll(defaultTo);

    for (ICommandListener listener : listeners) {
      addListener(listener);
    }
  }

  protected String printableValue(E o) {
    return String.valueOf(o);
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
    int beforeSize = size();
    wrapping.clear();
    if (size() != beforeSize) {
      callUpdateListeners();
    }
  }

  @Override
  public String toString() {
    return getName() +
        " = [" +
        wrapping.stream()
            .map(this::printableValue)
            .collect(Collectors.joining(", ")) +
        "]";
  }

  @Override
  public <T extends ICommandListener> List<T> getListeners(Class<T> type) {
    return (List<T>) listeners.get(type);
  }

  @Override
  public boolean addListeners(Class<? extends ICommandListener> type, Collection<? extends ICommandListener> listener) {
    return listeners.putAll(type, listener);
  }
}
