package com.matt.forgehax.util.task;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public interface TaskChain<E> extends Iterator<E> {
  TaskChain EMPTY =
      new TaskChain<Object>() {
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

  default boolean isEmpty() {
    return !hasNext();
  }

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
      return new TaskChain<T>() {
        final Queue<T> tasks = Queues.newArrayDeque(queue);

        @Override
        public boolean hasNext() {
          return !tasks.isEmpty();
        }

        @Override
        public T next() {
          return tasks.poll();
        }
      };
    }
  }
}
