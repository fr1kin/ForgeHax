package com.matt.forgehax.asm.reflection.type;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */
public class FastTypeBuilder {
    public static FastTypeBuilder create() {
        return new FastTypeBuilder();
    }

    private Class<?> insideClass = null;
    private String[] names = null;

    // method only
    private Class<?>[] parameters = null;

    public FastTypeBuilder setInsideClass(Class<?> insideClass) {
        this.insideClass = insideClass;
        return this;
    }

    public FastTypeBuilder setNames(String... names) {
        this.names = Arrays.copyOf(names, names.length);
        return this;
    }

    public FastTypeBuilder setParameters(Class<?>... parameters) {
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        return this;
    }

    public <V> FastField<V> asField() {
        Objects.requireNonNull(insideClass);
        Objects.requireNonNull(names);
        return new FastField<V>(insideClass, names);
    }

    public <V> FastMethod<V> asMethod() {
        Objects.requireNonNull(insideClass);
        Objects.requireNonNull(names);
        Objects.requireNonNull(parameters);
        return new FastMethod<V>(insideClass, names, parameters);
    }
}
