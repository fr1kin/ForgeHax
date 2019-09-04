package com.matt.forgehax.asm.utils.transforming;

import com.google.common.collect.Queues;
import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.asmtype.ASMMethod;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import joptsimple.internal.Strings;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created on 5/2/2017 by fr1kin
 */
public abstract class MethodTransformer implements ASMCommon {
  
  private final Collection<TaskElement> tasks = Queues.newPriorityQueue();
  
  public MethodTransformer() {
    for (Method m : getClass().getDeclaredMethods()) {
      try {
        m.setAccessible(true);
        if (m.isAnnotationPresent(Inject.class)
          && m.getParameterCount() > 0
          && MethodNode.class.equals(m.getParameterTypes()[0])) {
          tasks.add(
            new TaskElement(
              m,
              m.getAnnotation(Inject.class).description(),
              m.getAnnotation(Inject.class).priority()));
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
        ASMStackLogger.printStackTrace(e);
      }
    }
  }
  
  public final Collection<TaskElement> getTasks() {
    return Collections.unmodifiableCollection(tasks);
  }
  
  public abstract ASMMethod getMethod();
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof MethodTransformer
      && Objects.equals(getMethod(), ((MethodTransformer) obj).getMethod());
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MethodTransformer [");
    builder.append(getMethod() != null ? getMethod().toString() : "null");
    builder.append("] ");
    if (tasks.isEmpty()) {
      builder.append("No transform tasks");
    } else {
      builder.append("Found ");
      builder.append(tasks.size());
      builder.append(" transform tasks: ");
      Iterator<TaskElement> it = tasks.iterator();
      while (it.hasNext()) {
        TaskElement next = it.next();
        String desc = next.getMethod().getDeclaredAnnotation(Inject.class).description();
        if (!Strings.isNullOrEmpty(desc)) {
          builder.append(desc);
        }
        if (it.hasNext()) {
          builder.append(", ");
        }
      }
    }
    return builder.toString();
  }
  
  public static class TaskElement implements Comparable<TaskElement> {
    
    private final Method method;
    private final String description;
    private final InjectPriority priority;
    
    public TaskElement(Method method, String description, InjectPriority priority) {
      this.method = method;
      this.description = description;
      this.priority = priority;
    }
    
    public Method getMethod() {
      return method;
    }
    
    public String getDescription() {
      return description;
    }
    
    public InjectPriority getPriority() {
      return priority;
    }
    
    @Override
    public int compareTo(TaskElement o) {
      return priority.compareTo(o.priority);
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj instanceof TaskElement && method.equals(((TaskElement) obj).method);
    }
  }
}
