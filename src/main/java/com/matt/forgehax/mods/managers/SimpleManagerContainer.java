package com.matt.forgehax.mods.managers;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.common.PriorityEnum;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleManagerContainer<T> {
    private final List<Member<T>> functions = Lists.newArrayList();

    public SimpleManagerContainer() {}

    public boolean register(T func, PriorityEnum priority) {
        synchronized (functions) {
            boolean r = functions.add(new Member<>(func, priority));
            Collections.sort(functions);
            return r;
        }
    }
    public boolean register(T func) {
        return register(func, PriorityEnum.DEFAULT);
    }

    public boolean unregister(T func) {
        synchronized (functions) {
            return functions.remove(new Member<>(func, PriorityEnum.DEFAULT)); // create dummy member
        }
    }

    public List<T> functions() {
        synchronized (functions) {
            return functions.stream()
                    .map(Member::getFunction)
                    .collect(Collectors.toList());
        }
    }

    protected static class Member<E> implements Comparable<Member> {
        private final E function;
        private final PriorityEnum priority;

        private Member(E function, PriorityEnum priority) {
            Objects.requireNonNull(function);
            Objects.requireNonNull(priority);
            this.function = function;
            this.priority = priority;
        }

        public E getFunction() {
            return function;
        }

        public PriorityEnum getPriority() {
            return priority;
        }

        @Override
        public int compareTo(Member o) {
            return getPriority().compareTo(o.getPriority());
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(getFunction());
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof SimpleManagerContainer.Member && this.getFunction() == ((Member) obj).getFunction()); // compare references, don't use equals()
        }
    }
}
