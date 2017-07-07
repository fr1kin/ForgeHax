package com.matt.forgehax.util.task;

import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.mod.BaseMod;

/**
 * Created on 6/15/2017 by fr1kin
 */
public abstract class Task implements Comparable<Task> {
    private final BaseMod parent;

    private PriorityEnum priority;
    private boolean enabled = false;

    protected Task(BaseMod parent, PriorityEnum priority) {
        this.parent = parent;
        this.priority = priority;
    }

    public BaseMod getParent() {
        return parent;
    }

    public PriorityEnum getPriority() {
        return priority;
    }

    public void setPriority(PriorityEnum priority) {
        this.priority = priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void onPreUpdate();
    public abstract void onPostUpdate();

    public abstract void finished();

    @Override
    public int compareTo(Task o) {
        return getPriority().compareTo(o.getPriority());
    }
}
