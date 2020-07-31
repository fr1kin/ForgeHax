package dev.fiki.forgehax.asm.utils.transforming;

import dev.fiki.forgehax.api.mapper.ClassMapping;
import dev.fiki.forgehax.asm.utils.asmtype.ASMClass;
import lombok.Getter;
import org.objectweb.asm.Opcodes;

@Getter
public class Patch implements Opcodes {
  private ASMClass transformingClass;

  public Patch() {
    if (getClass().isAnnotationPresent(ClassMapping.class)) {
      ClassMapping mapping = getClass().getAnnotation(ClassMapping.class);
      this.transformingClass = ASMClass.unmap(mapping);
    }
  }
}
