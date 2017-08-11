package com.matt.forgehax.util.task.manager;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.task.TaskParent;
import com.matt.forgehax.util.task.TaskType;

import java.util.List;

/**
 * Created on 6/15/2017 by fr1kin
 */
public class TaskManager {
    private static final List<TaskParent> tasks = Lists.newCopyOnWriteArrayList();

    public static void register(TaskParent task) {
        tasks.add(task);
    }

    public static void unregister(TaskParent task) {
        tasks.remove(task);
    }

    public static TaskParent getTop(final TaskType type) {
        return null;
    }
}
