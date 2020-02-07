package dev.fiki.forgehax.main.util.cmd;

import dev.fiki.forgehax.main.util.serialization.IJsonSerializable;

import java.util.Map;

public interface ISettingMap<K, V, M extends Map<K, V>> extends IParentCommand, IJsonSerializable, Map<K, V> {

}
