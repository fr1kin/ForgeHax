package dev.fiki.forgehax.api.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.listener.ICommandListener;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractSettingMap<K, V, M extends Map<K, V>>
    extends AbstractParentCommand implements ISettingMap<K, V, M> {
  private final Multimap<Class<? extends ICommandListener>, ICommandListener> listeners =
      Multimaps.newListMultimap(Maps.newConcurrentMap(), Lists::newCopyOnWriteArrayList);

  protected final M wrapping;

  public AbstractSettingMap(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags,
      @NonNull Supplier<M> supplier, List<ICommandListener> listeners) {
    super(parent, name, aliases, description, flags);
    this.wrapping = supplier.get();

    for (ICommandListener listener : listeners) {
      addListener(listener);
    }
  }

  protected String printableKey(K o) {
    return String.valueOf(o);
  }

  protected String printableValue(V o) {
    return String.valueOf(o);
  }

  @Override
  public boolean removeKeys(Collection<? extends K> collection) {
    int beforeSize = size();
    collection.forEach(wrapping::remove);
    if (beforeSize != size()) {
      callUpdateListeners();
      return true;
    }
    return false;
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
  public boolean containsKey(Object o) {
    return wrapping.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return wrapping.containsValue(o);
  }

  @Override
  public V get(Object o) {
    return wrapping.get(o);
  }

  @Override
  public V put(K k, V v) {
    V ret = wrapping.put(k, v);
    callUpdateListeners();
    return ret;
  }

  @Override
  public V remove(Object o) {
    V ret = wrapping.remove(o);
    if (ret != null) {
      callUpdateListeners();
    }
    return ret;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    int beforeSize = size();
    wrapping.putAll(map);
    if (size() != beforeSize) {
      callUpdateListeners();
    }
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
  public Set<K> keySet() {
    return wrapping.keySet();
  }

  @Override
  public Collection<V> values() {
    return wrapping.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return wrapping.entrySet();
  }

  @Override
  public String toString() {
    return getName() +
        " = {" +
        wrapping.entrySet().stream()
            .map(entry -> printableKey(entry.getKey()) + " = " + printableValue(entry.getValue()))
            .collect(Collectors.joining(", ")) +
        "}";
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
