package dev.fiki.forgehax.asm.utils;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Objects;

public class InsnPattern {
  
  private final AbstractInsnNode first;
  private final AbstractInsnNode last;
  
  public InsnPattern(AbstractInsnNode first, AbstractInsnNode last) {
    this.first = first;
    this.last = last;
  }
  
  public <T extends AbstractInsnNode> T getFirst() {
    return (T) Objects.requireNonNull(this.first);
  }

  public <T extends AbstractInsnNode> T getFirst(String message) {
    return (T) Objects.requireNonNull(this.first, message);
  }
  
  public <T extends AbstractInsnNode> T getLast() {
    return (T) Objects.requireNonNull(this.last);
  }

  public <T extends AbstractInsnNode> T getLast(String message) {
    return (T) Objects.requireNonNull(this.last, message);
  }
  
  public <T extends AbstractInsnNode> T getIndex(final int index) {
    AbstractInsnNode node = this.first;
    for (int i = 0; i < index; i++) {
      node = node.getNext();
      // if (node == this.last && i < index)
      //    throw new ArrayIndexOutOfBoundsException(String.valueOf(index));
    }
    return (T) node;
  }
}
