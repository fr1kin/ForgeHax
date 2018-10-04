package com.matt.forgehax.asm.utils.asmtype;

import com.matt.forgehax.asm.utils.asmtype.builders.ASMBuilders;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMFieldBuilder;
import com.matt.forgehax.asm.utils.asmtype.builders.ASMMethodBuilder;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.name.IName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import org.objectweb.asm.Type;

/** Created on 5/26/2017 by fr1kin */
public class ASMClass implements IASMType {
  private final IName<Type> className;

  public ASMClass(IName<Type> className) {
    this.className = className;
  }

  public IName<Type> getAll() {
    return className;
  }

  /**
   * The class name for the class of the given state (packages separated by "." instead of "/")
   *
   * @return internal class name
   */
  @Override
  public String getNameByState(State state) {
    return className.getByStateSafe(state).getClassName();
  }

  @Override
  public String getDescriptorByState(State state) {
    return className.getByStateSafe(state).getDescriptor();
  }

  public String getInternalName() {
    return className.get().getInternalName();
  }

  public String getInternalNameByState(State state) {
    return className.getByStateSafe(state).getInternalName();
  }

  public String getRuntimeInternalName() {
    return getInternalNameByState(RuntimeState.getState());
  }

  /**
   * Creates a new ASMMethodBuilder and sets its parent class to this
   *
   * @return new ASMMethodBuilder instance
   */
  public ASMMethodBuilder childMethod() {
    return ASMBuilders.newMethodBuilder().setParentClass(this);
  }

  /**
   * Creates a new ASMFieldBuilder and sets its parent class to this
   *
   * @return new ASMFieldBuilder instance
   */
  public ASMFieldBuilder childField() {
    return ASMBuilders.newFieldBuilder().setParentClass(this);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ASMClass && Objects.equals(getName(), ((ASMClass) obj).getName());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("CLASS[states=%d]{", className.getStateCount()));
    Iterator<State> it = Arrays.asList(State.values()).iterator();
    boolean needsSeparator = false;
    while (it.hasNext()) {
      State next = it.next();
      Type type = className.getByState(next);
      if (type != null) {
        if (needsSeparator) builder.append(",");
        builder.append(next.name());
        builder.append("=");
        builder.append(type.getInternalName());
        needsSeparator = true;
      }
    }
    builder.append("}");
    return builder.toString();
  }
}
