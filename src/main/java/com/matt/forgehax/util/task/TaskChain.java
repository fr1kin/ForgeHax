package com.matt.forgehax.util.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public interface TaskChain<E> extends Iterator<E> {
  TaskChain EMPTY =
      new TaskChain<Object>() {
        @Override
        public TaskChain<Object> then(Object task) {
          throw new UnsupportedOperationException();
        }

        @Override
        public TaskChain<Object> thenLast(Object task) {
          throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Object next() {
          return null;
        }
      };

  static <T> Builder<T> builder() {
    return new Builder<>();
  }

  static <T> TaskChain<T> singleton(final T taskIn) {
    return new TaskChain<T>() {
      @Override
      public TaskChain<T> then(T task) {
        throw new UnsupportedOperationException();
      }

      @Override
      public TaskChain<T> thenLast(T task) {
        throw new UnsupportedOperationException();
      }

      T task = taskIn;

      @Override
      public boolean hasNext() {
        return task != null;
      }

      @Override
      public T next() {
        T ret = task;
        task = null;
        return ret;
      }
    };
  }

  static <T> TaskChain<T> empty() {
    return (TaskChain<T>) EMPTY;
  }

  TaskChain<E> then(E task);

  TaskChain<E> thenLast(E task);

  default boolean isEmpty() {
    return !hasNext();
  }

  // not really needed anymore
  class Builder<T> {
    private final Queue<T> queue = Queues.newArrayDeque();

    public Builder<T> then(T task) {
      queue.add(task);
      return this;
    }

    public Builder<T> addAll(Collection<T> tsks) {
      queue.addAll(tsks);
      return this;
    }

    public Builder<T> collect(TaskChain<T> ts) {
      while (ts.hasNext()) queue.add(ts.next());
      return this;
    }

    public TaskChain<T> build() {
      return new DynamicTaskChain<>(queue);
    }
  }

  class DynamicTaskChain<T> implements TaskChain<T> {
    private final List<T> tasks = Lists.newArrayList();

    private DynamicTaskChain() {}

    private DynamicTaskChain(Collection<T> collection) {
      tasks.addAll(collection);
    }

    @Override
    public TaskChain<T> then(T task) {
      tasks.add(0, task); // add to head
      return this;
    }

    @Override
    public TaskChain<T> thenLast(T task) {
      tasks.add(task); // add to tail
      return null;
    }

    @Override
    public boolean hasNext() {
      return !tasks.isEmpty();
    }

    @Override
    public T next() {
      return tasks.remove(0);
    }
  }
}
