package com.matt.forgehax.asm.utils.asmtype;

import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.name.IName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nullable;
import org.objectweb.asm.Type;

/** Created on 5/26/2017 by fr1kin */
public class ASMMethod extends ASMClassChild {
  private final IName<String> methodName;
  private final IName<Type>[] parameters;
  private final IName<Type> returnType;

  public ASMMethod(
      @Nullable ASMClass parentClass,
      IName<String> methodName,
      IName<Type>[] parameters,
      IName<Type> returnType) {
    super(parentClass);
    this.methodName = methodName;
    this.parameters = Arrays.copyOf(parameters, parameters.length);
    this.returnType = returnType;
  }

  /**
   * The method type, specified by state, containing the method name
   *
   * @param state state of the environment to get
   * @return type containing method name
   */
  @Override
  public String getNameByState(State state) {
    return methodName.getByStateSafe(state);
  }

  @Override
  public String getDescriptorByState(State state) {
    return Type.getMethodType(getReturnTypeByState(state), getArgumentTypesByState(state))
        .getDescriptor();
  }

  /**
   * An array of argument types for the normal method
   *
   * @return types
   */
  public Type[] getArgumentTypes() {
    return getArgumentTypesByState(State.NORMAL);
  }

  public Type[] getArgumentTypesByState(State state) {
    Type[] all = new Type[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      all[i] = parameters[i].getByStateSafe(state);
    }
    return all;
  }

  /**
   * Check if the state has a unique signature because a type in the arguments has multiple states
   *
   * @param state
   * @return
   */
  private boolean isArgumentStatePresent(State state) {
    for (int i = 0; i < parameters.length; i++) {
      Type arg = parameters[i].getByState(state);
      if (arg != null) return true;
    }
    return false;
  }

  /**
   * An array of argument types for the runtime method
   *
   * @return types
   */
  public Type[] getRuntimeArgumentTypes() {
    return getArgumentTypesByState(RuntimeState.getState());
  }

  /**
   * The return type for the normal method
   *
   * @return return type
   */
  public Type getReturnType() {
    return returnType.get();
  }

  public Type getReturnTypeByState(State state) {
    return returnType.getByStateSafe(state);
  }

  /**
   * The return type for the runtime method
   *
   * @return return type
   */
  public Type getRuntimeReturnType() {
    return getReturnTypeByState(RuntimeState.getState());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ASMMethod
        && Objects.equals(getName(), ((ASMMethod) obj).getName())
        && Objects.equals(getDescriptor(), ((ASMMethod) obj).getDescriptor());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    // if any member has another state the entire signature will change for that state
    // so we must find the maximum number of different states
    int maxStates = Math.max(methodName.getStateCount(), returnType.getStateCount());
    for (IName<Type> nm : parameters) maxStates = Math.max(maxStates, nm.getStateCount());
    builder.append(
        String.format("METHOD[states=%d,maxStates=%d]{", methodName.getStateCount(), maxStates));
    Iterator<State> it = Arrays.asList(State.values()).iterator();
    boolean needsSeparator = false;
    while (it.hasNext()) {
      State next = it.next();
      // if any are not null then a unique signature for this state exists
      if (methodName.getByState(next) != null
          || returnType.getByState(next) != null
          || isArgumentStatePresent(next)) {
        if (needsSeparator) builder.append(",");
        builder.append(next.name());
        builder.append("=");
        builder.append(getNameByState(next));
        builder.append(getDescriptorByState(next));
        needsSeparator = true;
      }
    }
    builder.append("}");
    return builder.toString();
  }
}
