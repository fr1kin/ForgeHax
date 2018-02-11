package com.matt.forgehax.util;

import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Created on 2/6/2018 by fr1kin
 */
public class ImmutableCollectors {
    // credits to http://www.nurkiewicz.com/2014/07/introduction-to-writing-custom.html

    public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
        return new ImmutableSetCollector<>();
    }

    private static class ImmutableSetCollector<E> implements Collector<E, ImmutableSet.Builder<E>, ImmutableSet<E>> {
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
            return (l, r) -> {l.addAll(r.build()); return l;};
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
}
