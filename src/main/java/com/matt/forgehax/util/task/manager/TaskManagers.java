package com.matt.forgehax.util.task.manager;

import com.matt.forgehax.util.task.ViewTask;

/**
 * Created on 6/15/2017 by fr1kin
 */
public class TaskManagers {
    private static final TaskManager<ViewTask> VIEW_MANAGER = new TaskManager<>();

    public static TaskManager<ViewTask> getViewManager() {
        return VIEW_MANAGER;
    }
}
