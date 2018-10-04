package com.matt.forgehax.util.task;

import static com.matt.forgehax.Helper.getLocalPlayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.matt.forgehax.util.common.PriorityEnum;
import java.util.function.Consumer;

/** Created on 8/5/2017 by fr1kin */
public class Task implements Comparable<Task> {
  private final Multimap<Type, Consumer<IProcess.DataEntry>> pre =
      Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);
  private final Multimap<Type, Consumer<IProcess.DataEntry>> post =
      Multimaps.newListMultimap(Maps.newHashMap(), Lists::newArrayList);

  private boolean active = false;
  private PriorityEnum priority = PriorityEnum.DEFAULT;

  public Task(
      Multimap<Type, Consumer<IProcess.DataEntry>> pre,
      Multimap<Type, Consumer<IProcess.DataEntry>> post) {
    this.pre.putAll(pre);
    this.post.putAll(post);
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

  public boolean hasTask(Type type) {
    return !pre.get(type).isEmpty() || !post.get(type).isEmpty();
  }

  public TaskProcessing newTaskProcessing(Type type) {
    return new TaskProcessing(this, type);
  }

  @Override
  public int compareTo(Task o) {
    return priority.compareTo(o.priority);
  }

  public enum Type implements IProcess {
    LOOK {
      @Override
      public void process(DataEntry data) {
        data.set("previousPitch", getLocalPlayer().rotationPitch);
        data.set("previousYaw", getLocalPlayer().rotationYaw);
      }
    },
    ;
  }

  public static class TaskProcessing {
    private final Task task;
    private final Type type;

    private final IProcess.DataEntry data = new IProcess.DataEntry();

    public TaskProcessing(Task task, Type type) {
      this.task = task;
      this.type = type;
      type.process(data);
    }

    public void preProcessing() {
      task.pre.get(type).forEach(consumer -> consumer.accept(data));
    }

    public void postProcessing() {
      task.post.get(type).forEach(consumer -> consumer.accept(data));
    }
  }
}
