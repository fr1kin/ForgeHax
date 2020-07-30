package dev.fiki.forgehax.mapper.util

import dev.fiki.forgehax.api.mapper.MappedFormat
import org.objectweb.asm.Type

class AnnotationValueList implements Map<String, Object> {
  private List<Object> list;

  AnnotationValueList(List<Object> list) {
    this.list = list != null ? list : Collections.emptyList();

    if (this.list.size() % 2 != 0) {
      throw new Error('value list is asymmetrical!')
    }
  }

  AnnotationValueList() {
    this(new ArrayList<Object>())
  }

  String getAsString(String key) {
    return get(key) as String
  }

  Boolean getAsBoolean(String key) {
    def o = get(key)
    return o != -1 ? o as Boolean : null
  }

  Type getAsType(String key) {
    return get(key) as Type
  }

  Type[] getAsTypeArray(String key) {
    return get(key) as Type[]
  }

  MappedFormat getAsMappedFormat(String key) {
    def o = get(key) as String[]
    if (o && o[0] == Type.getDescriptor(MappedFormat)) {
      return MappedFormat.valueOf(o[1])
    }
    return null
  }

  Object putAsClass(String key, Class<?> clazz) {
    return put(key, Type.getType(clazz))
  }

  Object putAsMappedFormat(String key, MappedFormat type) {
    return put(key, [Type.getDescriptor(MappedFormat), type.name()])
  }

  AnnotationValueList copy() {
    return new AnnotationValueList(Collections.unmodifiableList(new ArrayList<Object>(list)))
  }

  private def findKeyIndex(Object key) {
    def e = list.indexed().find { i, o -> i % 2 == 0 && o == key }
    return e ? e.key : -1
  }

  @Override
  int size() {
    return list.size() / 2
  }

  @Override
  boolean isEmpty() {
    return list.isEmpty()
  }

  @Override
  boolean containsKey(Object key) {
    return findKeyIndex(key) != -1
  }

  @Override
  boolean containsValue(Object value) {
    return list.indexed().find { i, o -> i % 2 == 1 && o == value }
  }

  @Override
  Object get(Object key) {
    def i = findKeyIndex(key)
    return i != -1 ? list.get(i + 1) : null
  }

  @Override
  Object put(String key, Object value) {
    def i = findKeyIndex(key)
    if (i != -1) {
      // already exists in the map
      list.set(i, key)
      return list.set(i + 1, value) // return the old value
    } else {
      // no key exists yet
      list.addAll([key, value])
      return null
    }
  }

  @Override
  Object remove(Object key) {
    def i = findKeyIndex(key)
    if (i != -1) {
      // remove the key
      list.removeAt(i)
      // since we just removed an element, the value will be at index i
      return list.removeAt(i)
    }
    return null
  }

  @Override
  void putAll(Map<? extends String, ?> m) {
    m.each { put(it.key, it.value) }
  }

  @Override
  void clear() {
    list.clear()
  }

  @Override
  Set<String> keySet() {
    return list.indexed().findAll { it.key % 2 == 0 }.collect { it.value }
  }

  @Override
  Collection<Object> values() {
    return list.indexed().findAll { it.key % 2 == 1 }.collect { it.value }
  }

  @Override
  Set<Entry<String, Object>> entrySet() {
    def set = new HashSet<Entry<String, Object>>()
    for (def i = 0; i + 1 < list.size(); i += 2) {
      def e = new AbstractMap.SimpleEntry(list.get(i), list.get(i + 1))
      set += e
    }
    return set
  }

  @Override
  String toString() {
    return entrySet().collect { it.key + ' = ' + it.value + ' (' + it.value.getClass().getSimpleName() + ')' }.join('; ')
  }
}
