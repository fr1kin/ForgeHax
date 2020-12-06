package dev.fiki.forgehax.asm.utils.transforming;

import dev.fiki.forgehax.api.asm.runtime.RtMapClass;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import lombok.Getter;
import org.objectweb.asm.Opcodes;

@Getter
public class Patch implements Opcodes {
  private ASMClass transformingClass;

  public Patch() {
    if (getClass().isAnnotationPresent(RtMapClass.class)) {
      RtMapClass mapping = getClass().getAnnotation(RtMapClass.class);
      this.transformingClass = ASMClass.from(mapping);
    }
  }
}
