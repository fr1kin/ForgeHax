package dev.fiki.forgehax.asm.utils.asmtype;

import dev.fiki.forgehax.api.asm.runtime.Format;
import dev.fiki.forgehax.api.asm.runtime.RtMapMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public interface ASMMethod {
  static ASMMethod from(@NonNull RtMapMethod map) {
    return new Primary(map);
  }

  ASMClass getParentClass();

  String getName();

  Type getDescriptor();

  Format getFormat();

  ASMMethod setFormat(@NonNull Format format);

  Stream<? extends ASMMethod> getDelegates();

  default String getDescriptorString() {
    return getDescriptor().getDescriptor();
  }

  default Type getReturnType() {
    return getDescriptor().getReturnType();
  }

  default Type[] getArgumentTypes() {
    return getDescriptor().getArgumentTypes();
  }

  default boolean anyNameEqual(String other) {
    return getDelegates().map(ASMMethod::getName).anyMatch(other::equals);
  }

  default boolean anyDescriptorEqual(String desc) {
    return getDelegates().map(ASMMethod::getDescriptorString).anyMatch(desc::equals);
  }

  default boolean matchesInvoke(int opcode, AbstractInsnNode node) {
    return node.getOpcode() == opcode
        && node instanceof MethodInsnNode
        && this.anyNameEqual(((MethodInsnNode) node).name)
        && this.anyDescriptorEqual(((MethodInsnNode) node).desc);
  }

  default boolean matchesStaticMethodNode(AbstractInsnNode node) {
    return matchesInvoke(Opcodes.INVOKESTATIC, node);
  }

  abstract class Base implements ASMMethod {
    @Override
    public String toString() {
      return getParentClass() + " :: " + getName() + getDescriptorString();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof ASMMethod) {
        ASMMethod other = (ASMMethod) obj;
        return this.getParentClass().equals(other.getParentClass())
            && this.getName().equals(other.getName())
            && this.getDescriptor().equals(other.getDescriptor());
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(getParentClass(), getName(), getDescriptor());
    }
  }

  @Getter
  final class Primary extends Base {
    private final ASMClass parentClass;
    private final Delegate[] delegates;

    public Primary(RtMapMethod map) {
      this.parentClass = ASMClass.from(map.parent());

      this.delegates = new Delegate[Format.values().length];
      this.delegates[Format.NORMAL.ordinal()] =
          new Delegate(this, map.name(), Type.getMethodType(map.descriptor()), Format.NORMAL);

      for (RtMapMethod.Alternative alt : map.alternatives()) {
        this.delegates[alt.format().ordinal()] =
            new Delegate(this, alt.name(), Type.getMethodType(alt.descriptor()), alt.format());
      }
    }

    @Override
    public String getName() {
      return setFormat(getFormat()).getName();
    }

    @Override
    public Type getDescriptor() {
      return setFormat(getFormat()).getDescriptor();
    }

    @Override
    public Format getFormat() {
      return ASMEnv.getCurrentClassFormat();
    }

    @Override
    public ASMMethod setFormat(@NonNull Format format) {
      return delegates[format.ordinal()];
    }

    @Override
    public Stream<? extends ASMMethod> getDelegates() {
      return Arrays.stream(delegates).filter(Objects::nonNull);
    }
  }

  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
  final class Delegate extends Base {
    private final Primary owner;
    private final String name;
    private final Type descriptor;
    private final Format format;

    @Override
    public ASMClass getParentClass() {
      return owner.getParentClass();
    }

    @Override
    public ASMMethod setFormat(@NonNull Format format) {
      return owner.setFormat(format);
    }

    @Override
    public Stream<? extends ASMMethod> getDelegates() {
      return owner.getDelegates();
    }
  }
}
