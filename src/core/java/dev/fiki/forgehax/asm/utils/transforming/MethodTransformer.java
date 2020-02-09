package dev.fiki.forgehax.asm.utils.transforming;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import dev.fiki.forgehax.common.asmtype.ASMMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

import static dev.fiki.forgehax.asm.ASMCommon.getLogger;

/**
 * Created on 5/2/2017 by fr1kin
 */
public abstract class MethodTransformer implements ITransformerProvider<MethodNode> {
  
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
              m.getAnnotation(Inject.class).value(),
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
    getLogger().debug("Transforming method {}::{}[{}]",
        getMethod().getParent().getClassName(),
        getMethod().getMcp(), getMethod().getMcpDescriptor());

    for(MethodTransformer.TaskElement task : tasks) {
      try {
        task.getMethod().invoke(this, input);
        getLogger().debug("Successfully transformed task \"{}\"", task.getDescription());
      } catch (Throwable t) {
        if(t instanceof InvocationTargetException) {
          // we don't care about the reflection error
          t = t.getCause();
        }
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
    return Stream.of(getMethod().toSrgTransformerTarget(), getMethod().toMcpTransformerTarget())
        .map(DistinctTarget::new)
        .distinct()
        .map(DistinctTarget::getTarget)
        .collect(Collectors.toSet());
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

  @Getter
  @AllArgsConstructor
  static class DistinctTarget {
    private final ITransformer.Target target;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DistinctTarget that = (DistinctTarget) o;
      return Objects.equals(target.getElementName(), that.target.getElementName())
          && Objects.equals(target.getElementDescriptor(), that.target.getElementDescriptor());
    }

    @Override
    public int hashCode() {
      return Objects.hash(target.getElementName(), target.getElementDescriptor());
    }

    @Override
    public String toString() {
      return target.getClassName() + "::" +  target.getElementName() + target.getElementDescriptor();
    }
  }
}
