package com.matt.forgehax.asm.utils;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.Objects;

public class InsnPattern {

  private final AbstractInsnNode first;
  private final AbstractInsnNode last;

  public InsnPattern(AbstractInsnNode first, AbstractInsnNode last) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(last);
    this.first = first;
    this.last = last;
  }

  public <T extends AbstractInsnNode> T getFirst() {
    return (T)this.first;
  }

  public <T extends AbstractInsnNode> T getLast() {
    return (T)this.last;
  }

  public <T extends AbstractInsnNode> T getIndex(final int index) {
    AbstractInsnNode node = this.first;
    for (int i = 0; i < index; i++) {
      node = node.getNext();
      //if (node == this.last && i < index)
      //    throw new ArrayIndexOutOfBoundsException(String.valueOf(index));
    }
    return (T)node;
  }
}