package com.matt.forgehax.util.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.matt.forgehax.util.common.PriorityEnum;
import com.matt.forgehax.util.task.manager.TaskManager;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.matt.forgehax.Helper.getLocalPlayer;

/**
 * Created on 8/5/2017 by fr1kin
 */
public class TaskParent implements Comparable<TaskParent> {
    private final Multimap<TaskType, Consumer<DataEntry>> tasks = Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);

    private boolean active = false;
    private PriorityEnum priority = PriorityEnum.DEFAULT;

    public TaskParent(Multimap<TaskType, Consumer<DataEntry>> tasks) {
        this.tasks.putAll(tasks);
    }

    public void start() {
        active = true;
        TaskManager.register(this);
    }

    public void stop() {
        TaskManager.unregister(this);
        active = false;
    }

    public boolean isActivated() {
        return active;
    }

    public void setPriority(PriorityEnum priority) {
        this.priority = priority;
    }

    public boolean tasksExist(TaskType type) {
        return !tasks.get(type).isEmpty();
    }

    public List<TaskType> getAllTypes() {
        final List<TaskType> types = Lists.newArrayList();
        tasks.asMap().forEach((type, consumers) -> {
            if(!consumers.isEmpty()) types.add(type);
        });
        return types;
    }

    public void processLook() {
        final DataEntry entry = new DataEntry();
        entry.set("previousPitch", getLocalPlayer().rotationPitch);
        entry.set("previousYaw", getLocalPlayer().rotationYaw);

        tasks.get(TaskType.LOOK).forEach(consumer -> consumer.accept(entry));
    }

    @Override
    public int compareTo(TaskParent o) {
        return priority.compareTo(o.priority);
    }

    public static class DataEntry {
        private final Map<String, Object> data = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

        public <T> T getOrDefault(String o, T defaultValue) {
            try {
                return (T) data.get(o);
            } catch (Throwable t) {
                return defaultValue;
            }
        }

        public <T> T get(String o) {
            return getOrDefault(o, null);
        }

        public void set(String name, Object o) {
            data.put(name, o);
        }
    }
}
