package dev.fiki.forgehax.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

// credits to http://www.nurkiewicz.com/2014/07/introduction-to-writing-custom.html

/**
 * Created on 2/6/2018 by fr1kin
 */
public class Immutables {
  
  public static <T> Collection<T> copy(@Nullable Collection<T> collection) {
    return copyToList(collection);
  }
  
  public static <T> List<T> copyToList(@Nullable Collection<T> collection) {
    if (collection == null || collection.isEmpty()) {
      return Collections.emptyList();
    }
    if (collection instanceof ImmutableList) {
      return (List<T>) collection;
    } else if (collection.size() == 1) {
      return Collections.singletonList(collection.iterator().next());
    } else {
      return ImmutableList.copyOf(collection);
    }
  }
  
  public static <T> Set<T> copyToSet(@Nullable Collection<T> collection) {
    if (collection == null || collection.isEmpty()) {
      return Collections.emptySet();
    } else if (collection.size() == 1) {
      return Collections.singleton(collection.iterator().next());
    } else {
      return ImmutableSet.copyOf(collection);
    }
  }
  
  public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
    return new ImmutableSetCollector<>();
  }
  
  public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
    return new ImmutableListCollector<>();
  }
  
  private static class ImmutableSetCollector<E>
      implements Collector<E, ImmutableSet.Builder<E>, ImmutableSet<E>> {
    
    @Override
    public Supplier<ImmutableSet.Builder<E>> supplier() {
      return ImmutableSet::builder;
    }
    
    @Override
    public BiConsumer<ImmutableSet.Builder<E>, E> accumulator() {
      return ImmutableSet.Builder::add;
    }
    
    @Override
    public BinaryOperator<ImmutableSet.Builder<E>> combiner() {
      return (l, r) -> {
        l.addAll(r.build());
        return l;
      };
    }
    
    @Override
    public Function<ImmutableSet.Builder<E>, ImmutableSet<E>> finisher() {
      return ImmutableSet.Builder::build;
    }
    
    @Override
    public Set<Characteristics> characteristics() {
      return EnumSet.of(Characteristics.UNORDERED);
    }
  }
  
  private static class ImmutableListCollector<E>
      implements Collector<E, ImmutableList.Builder<E>, ImmutableList<E>> {
    
    @Override
    public Supplier<ImmutableList.Builder<E>> supplier() {
      return ImmutableList::builder;
    }
    
    @Override
    public BiConsumer<ImmutableList.Builder<E>, E> accumulator() {
      return ImmutableList.Builder::add;
    }
    
    @Override
    public BinaryOperator<ImmutableList.Builder<E>> combiner() {
      return (l, r) -> {
        l.addAll(r.build());
        return l;
      };
    }
    
    @Override
    public Function<ImmutableList.Builder<E>, ImmutableList<E>> finisher() {
      return ImmutableList.Builder::build;
    }
    
    @Override
    public Set<Characteristics> characteristics() {
      return Collections.emptySet();
    }
  }
}
