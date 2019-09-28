package com.matt.forgehax.asm.utils.asmtype;

import com.matt.forgehax.asm.utils.environment.State;
import com.matt.forgehax.asm.utils.name.IName;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nullable;
import org.objectweb.asm.Type;

/**
 * Created on 5/26/2017 by fr1kin
 */
public class ASMField extends ASMClassChild {
  
  private final IName<String> fieldName;
  private final IName<Type> type;
  
  public ASMField(@Nullable ASMClass parentClass, IName<String> fieldName, IName<Type> type) {
    super(parentClass);
    this.fieldName = fieldName;
    this.type = type;
  }
  
  /**
   * The field type, specified by state, containing the field name
   *
   * @param state state of the environment to get
   * @return type containing field name
   */
  @Override
  public String getNameByState(State state) {
    return fieldName.getByStateSafe(state);
  }
  
  @Override
  public String getDescriptorByState(State state) {
    return type.getByStateSafe(state).getDescriptor();
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof ASMField
      && Objects.equals(getName(), ((ASMField) obj).getName())
      && Objects.equals(getDescriptor(), ((ASMField) obj).getDescriptor());
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(
      String.format(
        "FIELD[states=%d,maxStates=%d]{",
        fieldName.getStateCount(), Math.max(fieldName.getStateCount(), type.getStateCount())));
    Iterator<State> it = Arrays.asList(State.values()).iterator();
    boolean needsSeparator = false;
    while (it.hasNext()) {
      State next = it.next();
      if (fieldName.getByState(next) != null || type.getByState(next) != null) {
        if (needsSeparator) {
          builder.append(",");
        }
        builder.append(next.name());
        builder.append("=");
        builder.append(getNameByState(next));
        builder.append(":");
        builder.append(getDescriptorByState(next));
        needsSeparator = true;
      }
    }
    builder.append("}");
    return builder.toString();
  }
}
