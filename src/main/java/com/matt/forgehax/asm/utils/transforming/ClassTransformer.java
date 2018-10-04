package com.matt.forgehax.asm.utils.transforming;

import static com.matt.forgehax.asm.utils.ASMStackLogger.printStackTrace;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.ASMCommon;
import com.matt.forgehax.asm.TypesMc;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.asmtype.ASMClass;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.mcversion.MCVersionChecker;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class ClassTransformer implements ASMCommon, TypesMc {
  private final ASMClass transformingClass;
  private final List<MethodTransformer> methodTransformers = Lists.newArrayList();

  public ClassTransformer(ASMClass clazz) {
    this.transformingClass = clazz;
    for (Class c : getClass().getDeclaredClasses()) {
      try {
        if (c.isAnnotationPresent(RegisterMethodTransformer.class)
            && MethodTransformer.class.isAssignableFrom(c)
            && MCVersionChecker.checkVersion(c)) {
          Constructor constructor;
          try {
            constructor = c.getDeclaredConstructor(getClass());
            constructor.setAccessible(true);
            MethodTransformer t = (MethodTransformer) constructor.newInstance(this);
            registerMethodPatch(t);
          } catch (NoSuchMethodException e) {
            constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            MethodTransformer t = (MethodTransformer) constructor.newInstance();
            registerMethodPatch(t);
          }
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
        ASMStackLogger.printStackTrace(e);
      }
    }
  }

  /**
   * This method will return the class obfuscation state.
   *
   * @return the state the Minecraft class/method/field obfuscation
   */
  public State getClassObfuscationState() {
    return RuntimeState.getDefaultState();
  }

  public void registerMethodPatch(MethodTransformer transformer) {
    methodTransformers.add(transformer);
  }

  public ASMClass getTransformingClass() {
    return transformingClass;
  }

  public String getTransformingClassName() {
    return transformingClass.getName();
  }

  public final void transform(final ClassNode node) {
    RuntimeState.setState(getClassObfuscationState());
    try {
      for (final MethodNode methodNode : node.methods)
        methodTransformers
            .stream()
            .filter(
                t ->
                    Objects.equals(t.getMethod().getRuntimeName(), methodNode.name)
                        && Objects.equals(t.getMethod().getRuntimeDescriptor(), methodNode.desc))
            .forEach(
                t ->
                    t.getTasks()
                        .forEach(
                            task -> {
                              try {
                                task.getMethod().invoke(t, methodNode);
                                // if we have gotten this far the transformation should have been
                                // successful
                                StringBuilder builder = new StringBuilder();
                                builder.append("Successfully transformed the task \"");
                                builder.append(task.getDescription());
                                builder.append("\" for ");
                                builder.append(getTransformingClassName());
                                builder.append("::");
                                builder.append(t.getMethod().getName());
                                LOGGER.info(builder.toString());
                              } catch (Throwable e) {
                                if (e instanceof InvocationTargetException) e = e.getCause();
                                StringBuilder builder = new StringBuilder();
                                builder.append(e.getClass().getSimpleName()); // exception name
                                builder.append(" thrown from ");
                                builder.append(getTransformingClassName());
                                builder.append("::");
                                builder.append(t.getMethod().getName());
                                builder.append(" for the task with the description \"");
                                builder.append(task.getDescription());
                                builder.append("\": ");
                                builder.append(e.getMessage());
                                LOGGER.error(builder.toString());
                                printStackTrace(e);
                              }
                            }));
    } finally {
      RuntimeState.releaseState();
    }
  }
}
