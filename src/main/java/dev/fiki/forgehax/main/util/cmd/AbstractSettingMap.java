package dev.fiki.forgehax.main.util.cmd;

import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractSettingMap<K, V, M extends Map<K, V>>
    extends AbstractParentCommand implements ISettingMap<K, V, M> {
  protected final M wrapping;

  public AbstractSettingMap(IParentCommand parent,
      String name, Collection<String> aliases, String description,
      Collection<EnumFlag> flags,
      @NonNull Supplier<M> supplier) {
    super(parent, name, aliases, description, flags);
    this.wrapping = supplier.get();
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
    callUpdateListeners();
    return ret;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    wrapping.putAll(map);
    callUpdateListeners();
  }

  @Override
  public void clear() {
    wrapping.clear();
    callUpdateListeners();
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
            .map(entry -> entry.getKey() + " = " + entry.getValue())
            .collect(Collectors.joining(", ")) +
        "}";
  }
}
