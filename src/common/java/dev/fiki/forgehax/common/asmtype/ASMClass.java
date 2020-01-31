package dev.fiki.forgehax.common.asmtype;

import lombok.*;
import org.objectweb.asm.Type;

/**
 * Created on 5/26/2017 by fr1kin
 */

@Getter
@Setter(value = AccessLevel.PACKAGE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ASMClass {
  private String className;

  public String getClassDescriptor() {
    return Type.getObjectType(getClassName()).getDescriptor();
  }

  public ASMField.ASMFieldBuilder newChildField() {
    return ASMField.builder().parent(this);
  }

  public ASMMethod.ASMMethodBuilder newChildMethod() {
    return ASMMethod.builder().parent(this);
  }
}
