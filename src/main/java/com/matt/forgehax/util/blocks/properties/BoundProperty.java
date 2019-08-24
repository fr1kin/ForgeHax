package com.matt.forgehax.util.blocks.properties;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * Created on 5/21/2017 by fr1kin
 */
public class BoundProperty implements IBlockProperty {

  private static final String HEADING = "bounds";

  private final Collection<Bound> bounds = Sets.newHashSet();

  public boolean add(int minY, int maxY) {
    return bounds.add(new Bound(minY, maxY));
  }

  public boolean remove(int minY, int maxY) {
    Bound bound = get(minY, maxY);
    return bound != null && bounds.remove(bound);
  }

  @Nullable
  public Bound get(int minY, int maxY) {
    for (Bound bound : bounds) {
      if (bound.getMin() == minY && bound.getMax() == maxY) {
        return bound;
      }
    }
    return null;
  }

  public Collection<Bound> getAll() {
    return Collections.unmodifiableCollection(bounds);
  }

  public boolean isWithinBoundaries(int posY) {
    if (bounds.isEmpty()) {
      return true;
    } else {
      for (Bound bound : bounds) {
        if (bound.isWithinBound(posY)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public void serialize(JsonWriter writer) throws IOException {
    writer.beginArray();
    for (Bound bound : bounds) {
      writer.beginArray();
      writer.value(bound.getMin());
      writer.value(bound.getMax());
      writer.endArray();
    }
    writer.endArray();
  }

  @Override
  public void deserialize(JsonReader reader) throws IOException {
    reader.beginArray();
    while (reader.hasNext()) {
      reader.beginArray();
      add(reader.nextInt(), reader.nextInt());
      reader.endArray();
    }
    reader.endArray();
  }

  @Override
  public boolean isNecessary() {
    return !bounds.isEmpty();
  }

  @Override
  public String helpText() {
    final StringBuilder builder = new StringBuilder("{");
    Iterator<Bound> it = bounds.iterator();
    while (it.hasNext()) {
      Bound bound = it.next();
      builder.append('[');
      builder.append(bound.getMin());
      builder.append(',');
      builder.append(bound.getMax());
      builder.append(']');
      if (it.hasNext()) {
        builder.append(", ");
      }
    }
    builder.append('}');
    return builder.toString();
  }

  @Override
  public IBlockProperty newImmutableInstance() {
    return new ImmutableBoundProperty();
  }

  @Override
  public String toString() {
    return HEADING;
  }

  public static class Bound {
  
    private final int min;
    private final int max;

    public Bound(int min, int max) throws IllegalArgumentException {
      if (min > max) {
        throw new IllegalArgumentException("min cannot be greater than max");
      }
      this.min = min;
      this.max = max;
    }

    public int getMin() {
      return min;
    }

    public int getMax() {
      return max;
    }

    public boolean isWithinBound(int y) {
      return y >= min && y <= max;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Bound && max == ((Bound) obj).max && min == ((Bound) obj).min;
    }
  }

  private static class ImmutableBoundProperty extends BoundProperty {
  
    @Override
    public boolean add(int minY, int maxY) {
      return false;
    }

    @Override
    public boolean remove(int minY, int maxY) {
      return false;
    }

    @Override
    public Bound get(int minY, int maxY) {
      return null;
    }

    @Override
    public Collection<Bound> getAll() {
      return Collections.emptySet();
    }

    @Override
    public boolean isWithinBoundaries(int posY) {
      return true; // Always return true if empty
    }
  }
}
