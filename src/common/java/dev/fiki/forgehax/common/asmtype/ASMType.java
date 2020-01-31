package dev.fiki.forgehax.common.asmtype;


import lombok.AllArgsConstructor;
import org.objectweb.asm.Type;

/**
 * Don't really need this unless I need to bring back obfuscated mappings
 */
abstract class ASMType {

  abstract Type getMcp();
  abstract Type getSrg();

  @AllArgsConstructor
  static class ObjectWebType extends ASMType {
    private final Type object;

    @Override
    Type getMcp() {
      return object;
    }

    @Override
    Type getSrg() {
      return getMcp();
    }
  }

  @AllArgsConstructor
  static class ASMClassType extends ASMType {
    private final ASMClass object;

    @Override
    Type getMcp() {
      return Type.getObjectType(object.getClassName());
    }

    @Override
    Type getSrg() {
      return getMcp();
    }
  }
}
