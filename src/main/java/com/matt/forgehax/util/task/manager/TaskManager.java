package com.matt.forgehax.util.task.manager;

import com.google.common.collect.Queues;
import com.matt.forgehax.util.task.Task;

import java.util.Queue;

/**
 * Created on 6/15/2017 by fr1kin
 */
public class TaskManager<T extends Task> {
    private final Queue<T> tasks = Queues.newPriorityQueue();

    public void registerTask(T task) {
        tasks.add(task);
    }

    public void unregisterTask(T task) {
        tasks.remove(task);
    }

    public T getTopTask() {
        return tasks.stream()
                .filter(Task::isEnabled)
                .findFirst()
                .orElse(null);
    }
}
