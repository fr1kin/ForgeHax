package com.matt.forgehax.util.command;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.exception.CommandBuildException;
import com.matt.forgehax.util.console.ConsoleIO;
import com.matt.forgehax.util.serialization.ISerializableJson;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 6/4/2017 by fr1kin
 */
public class Options<E extends ISerializableJson> extends Command
  implements Collection<E>, ISerializableJson {
  
  public static final String SUPPLIER = "Options.supplier";
  public static final String FACTORY = "Options.factory";
  public static final String DEFAULTS = "Options.defaults";
  
  private final Collection<E> contents;
  private final Function<String, E> factory;
  
  private final Collection<E> defaults;
  
  @SuppressWarnings("unchecked")
  protected Options(Map<String, Object> data) throws CommandBuildException {
    super(data);
    try {
      Supplier<Collection<E>> supplier = (Supplier<Collection<E>>) data.get(SUPPLIER);
      Objects.requireNonNull(supplier, "Missing supplier");
  
      this.contents = supplier.get();
      this.factory = (Function<String, E>) data.get(FACTORY);
  
      Supplier<Collection<E>> defaults = (Supplier<Collection<E>>) data.get(DEFAULTS);
      if (defaults != null) {
        this.defaults = supplier.get();
        this.defaults.addAll(defaults.get());
        this.contents.addAll(defaults.get());
      } else {
        this.defaults = Collections.emptyList();
      }
  
    } catch (Throwable t) {
      throw new CommandBuildException("Failed to build options", t);
    }
  }
  
  @Override
  protected boolean preprocessor(String[] args) {
    if (args.length > 0) {
      String opt = args[0];
      if (opt.matches("-r|--reset")) {
        contents.clear();
        contents.addAll(defaults);
        ConsoleIO.write(getName() + " reset");
        return false;
      }
    }
    return true;
  }
  
  public Collection<E> contents() {
    return contents;
  }
  
  public E get(Object o) {
    for (E element : this) {
      if (Objects.equals(element, o)) {
        return element;
      }
    }
    return null;
  }
  
  @Override
  public int size() {
    return contents.size();
  }
  
  @Override
  public boolean isEmpty() {
    return contents.isEmpty();
  }
  
  @Override
  public boolean contains(Object o) {
    return contents.contains(o);
  }
  
  @Override
  public Iterator<E> iterator() {
    return contents.iterator();
  }
  
  @Override
  public Object[] toArray() {
    return contents.toArray();
  }
  
  @Override
  public <T> T[] toArray(T[] a) {
    return contents.toArray(a);
  }
  
  @Override
  public boolean add(E e) {
    return contents.add(e);
  }
  
  @Override
  public boolean remove(Object o) {
    return contents.remove(o);
  }
  
  @Override
  public boolean containsAll(Collection<?> c) {
    return contents.containsAll(c);
  }
  
  @Override
  public boolean addAll(Collection<? extends E> c) {
    return contents.addAll(c);
  }
  
  @Override
  public boolean removeAll(Collection<?> c) {
    return contents.removeAll(c);
  }
  
  @Override
  public boolean retainAll(Collection<?> c) {
    return contents.retainAll(c);
  }
  
  @Override
  public void clear() {
    contents.clear();
  }
  
  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginObject();
    
    writer.name("data");
    writer.beginObject();
    for (E element : contents) {
      writer.name(element.toString());
      element.serialize(writer);
    }
    writer.endObject();
    
    writer.endObject();
  }
  
  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginObject();
    
    reader.nextName(); // data
    reader.beginObject();
    clear(); // clear current contents
    while (reader.hasNext()) {
      String name = reader.nextName();
      E element = factory.apply(name);
      if (element != null) {
        element.deserialize(reader);
        add(element);
      }
    }
    reader.endObject();
    
    reader.endObject();
  }
  
  @Override
  public String toString() {
    return getAbsoluteName();
  }
}
