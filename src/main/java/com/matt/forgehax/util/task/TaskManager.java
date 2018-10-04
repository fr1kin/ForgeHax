package com.matt.forgehax.util.task;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

/** Created on 6/15/2017 by fr1kin */
public class TaskManager {
  private static final List<Task> tasks = Lists.newCopyOnWriteArrayList();

  public static void register(Task task) {
    tasks.add(task);
  }

  public static void unregister(Task task) {
    tasks.remove(task);
  }

  @Nullable
  public static Task.TaskProcessing getTop(final Task.Type type) {
    Optional<Task> t =
        tasks
            .stream()
            .filter(Task::isActivated)
            .filter(task -> task.hasTask(type))
            .sorted()
            .findFirst();
    return t.isPresent() ? t.get().newTaskProcessing(type) : null;
  }
}
