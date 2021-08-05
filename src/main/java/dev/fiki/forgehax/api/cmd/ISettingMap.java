package dev.fiki.forgehax.api.cmd;

import dev.fiki.forgehax.api.serialization.IJsonSerializable;

import java.util.Collection;
import java.util.Map;

public interface ISettingMap<K, V, M extends Map<K, V>> extends IParentCommand, IJsonSerializable, Map<K, V> {
  boolean removeKeys(Collection<? extends K> collection);
}
