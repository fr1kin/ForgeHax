package com.matt.forgehax.util.task;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.common.PriorityEnum;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleManagerContainer<T> {
  private final List<Member<T>> functions = Lists.newArrayList();
  private final List<Listener<T>> listeners = Lists.newArrayList();

  public SimpleManagerContainer() {}

  private Optional<Member<T>> get(T func) {
    synchronized (functions) {
      return functions
          .stream()
          .filter(member -> member.getFunction() == func) // compare references
          .findFirst();
    }
  }

  private Optional<Member<T>> find(Predicate<Member<T>> predicate) {
    synchronized (functions) {
      return functions.stream().filter(predicate).findFirst();
    }
  }

  private boolean register(T func, PriorityEnum priority, boolean once) {
    synchronized (functions) {
      if (get(func).isPresent()) throw new IllegalArgumentException("function already registered");
      synchronized (listeners) {
        listeners.forEach(l -> l.onRegister(func));
      }
      boolean r = functions.add(new Member<>(func, priority, once));
      Collections.sort(functions);
      return r;
    }
  }

  public boolean register(T func, PriorityEnum priority) {
    return register(func, priority, false);
  }

  public boolean register(T func) {
    return register(func, PriorityEnum.DEFAULT);
  }

  public boolean registerTemporary(T func, PriorityEnum priority) {
    return register(func, priority, true);
  }

  public boolean registerTemporary(T func) {
    return registerTemporary(func, PriorityEnum.DEFAULT);
  }

  private void unregister(Member<T> member) {
    synchronized (functions) {
      synchronized (listeners) {
        listeners.forEach(l -> l.onUnregister(member.getFunction()));
      }
      functions.remove(member);
    }
  }

  public void unregister(T func) {
    synchronized (functions) {
      get(func).ifPresent(this::unregister);
    }
  }

  public boolean registerListener(Listener<T> listener) {
    synchronized (listeners) {
      return listeners.add(listener);
    }
  }

  public boolean unregisterListener(Listener<T> listener) {
    synchronized (listeners) {
      return listeners.remove(listener);
    }
  }

  public List<T> functions() {
    synchronized (functions) {
      return functions.stream().map(Member::getFunction).collect(Collectors.toList());
    }
  }

  private void setRunning(Member<T> member, boolean state) {
    synchronized (listeners) {
      if (state) {
        listeners.forEach(l -> l.onFunctionStarted(member.getFunction()));
        member.setRunning(true);
      } else {
        listeners.forEach(l -> l.onFunctionStarted(member.getFunction()));
        member.setRunning(false);
      }
    }
  }

  public void begin(T function) {
    synchronized (functions) {
      Member<T> member = get(function).orElse(null);

      if (member == null) return;

      find(Member::isRunning)
          .ifPresent(
              m -> {
                synchronized (listeners) {
                  listeners.forEach(l -> l.onFunctionStopped(m.getFunction()));
                }
                m.setRunning(false);
              });

      setRunning(member, true);
    }
  }

  public void finish(T function) {
    synchronized (functions) {
      Member<T> member = get(function).orElse(null);

      if (member == null || !member.isRunning()) return;

      setRunning(member, false);

      if (member.isOnce()) unregister(member);
    }
  }

  protected static class Member<E> implements Comparable<Member> {
    private final E function;
    private final PriorityEnum priority;
    private final boolean once;

    private boolean running = false;

    private Member(E function, PriorityEnum priority, boolean once) {
      Objects.requireNonNull(function);
      Objects.requireNonNull(priority);
      this.function = function;
      this.priority = priority;
      this.once = once;
    }

    public E getFunction() {
      return function;
    }

    public PriorityEnum getPriority() {
      return priority;
    }

    private boolean isOnce() {
      return once;
    }

    public boolean isRunning() {
      return running;
    }

    private void setRunning(boolean running) {
      this.running = running;
    }

    @Override
    public int compareTo(Member o) {
      return (isOnce() || o.isOnce())
          ? Boolean.compare(isOnce(), o.isOnce())
          : getPriority().compareTo(o.getPriority());
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(getFunction());
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof SimpleManagerContainer.Member
              && this.getFunction()
                  == ((Member) obj).getFunction()); // compare references, don't use equals()
    }
  }

  public interface Listener<E> {
    void onRegister(E function);

    void onUnregister(E function);

    void onFunctionStarted(E function);

    void onFunctionStopped(E function);
  }
}
