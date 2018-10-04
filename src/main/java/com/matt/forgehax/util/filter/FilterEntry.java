package com.matt.forgehax.util.filter;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.Helper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.List;
import joptsimple.internal.Strings;

/** Created on 8/23/2017 by fr1kin */
public class FilterEntry<E extends FilterElement> implements ISerializableJson {
  private final String name;
  private final List<E> filtered = Lists.newArrayList();

  private String tag = Strings.EMPTY;
  private int color = Utils.Colors.WHITE;

  public FilterEntry(String name) {
    this.name = name;
  }

  public boolean add(E element) {
    return element != null && !contains(element) && filtered.add(element);
  }

  public boolean remove(E element) {
    return element != null && filtered.remove(element);
  }

  @SuppressWarnings("unchecked")
  public E get(String name) {
    for (E e : filtered) if (e.equals(name)) return e;
    return null;
  }

  public boolean contains(String name) {
    return get(name) != null;
  }

  public boolean contains(FilterElement element) {
    return filtered.contains(element);
  }

  public boolean isFiltered(Object o) {
    for (FilterElement e : filtered) if (e.equals(o)) return true;
    return false;
  }

  public String getName() {
    return name;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public int getColor() {
    return color;
  }

  public int[] getColor4() {
    return Utils.toRGBAArray(color);
  }

  public void setColor(int color) {
    this.color = color;
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();

    writer.name("tag");
    writer.value(tag);

    writer.name("color");
    writer.value(color);

    writer.name("filtered");
    writer.beginObject();
    for (FilterElement e : filtered) {
      writer.value(e.name());
      e.serialize(writer);
    }
    writer.endObject();

    writer.endObject();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "tag":
          setTag(reader.nextString());
          break;
        case "color":
          setColor(reader.nextInt());
          break;
        case "filtered":
          reader.beginObject();
          // TODO: factory
          while (reader.hasNext()) {
            try {
              E instance = FilterFactory.newInstanceByName(reader.nextName());
              add(instance);
            } catch (Throwable t) {
              Helper.handleThrowable(t);
            }
          }
          reader.endObject();
          break;
      }
    }

    reader.endObject();
  }

  @Override
  public String toString() {
    return super.toString();
  }
}
