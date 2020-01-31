package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.fiki.forgehax.common.asmtype.ASMMethod;
import java.lang.reflect.Method;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public abstract class MethodTransformer implements ITransformer<MethodNode> {
  
  private final Queue<TaskElement> tasks = new PriorityQueue<>();
  
  public MethodTransformer() {
    for (Method m : getClass().getDeclaredMethods()) {
      try {
        m.setAccessible(true);
        if (m.isAnnotationPresent(Inject.class)
          && m.getParameterCount() > 0
          && MethodNode.class.equals(m.getParameterTypes()[0])) {
          tasks.add(
            new TaskElement(m,
              m.getAnnotation(Inject.class).description(),
              m.getAnnotation(Inject.class).priority()));
        }
      } catch (Throwable e) {
        getLogger().error("Error building method transformer task");
        getLogger().error(e);
      }
    }
  }
  
  public final Queue<TaskElement> getTasks() {
    return tasks;
  }
  
  public abstract ASMMethod getMethod();
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof MethodTransformer
      && Objects.equals(getMethod(), ((MethodTransformer) obj).getMethod());
  }

  @Nonnull
  @Override
  public MethodNode transform(MethodNode input, ITransformerVotingContext context) {
    for(MethodTransformer.TaskElement task : tasks) {
      try {
        task.getMethod().invoke(task, input);
      } catch (Throwable t) {
        // catch errors
        getLogger().error("Failed to transform task \"{}\" in method {}::{}[{}]",
            task.getDescription(),
            getMethod().getParent().getClassName(),
            getMethod().getMcp(), getMethod().getMcpDescriptor());
        getLogger().error(t, t);
      }
    }
    return input;
  }

  @Nonnull
  @Override
  public TransformerVoteResult castVote(ITransformerVotingContext context) {
    return TransformerVoteResult.YES;
  }

  @Nonnull
  @Override
  public Set<Target> targets() {
    return Collections.singleton(ITransformer.Target.targetMethod(
        getMethod().getParent().getClassName(),
        getMethod().getSrg(),
        getMethod().getSrgDescriptor()));
  }

  @Getter
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class TaskElement implements Comparable<TaskElement> {
    private final Method method;

    @EqualsAndHashCode.Exclude
    private final String description;

    @EqualsAndHashCode.Exclude
    private final InjectPriority priority;
    
    @Override
    public int compareTo(TaskElement o) {
      return priority.compareTo(o.priority);
    }
  }
}
