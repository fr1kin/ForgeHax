package dev.fiki.forgehax.asm.utils;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ASMPattern {

  public static Builder builder() {
    return new Builder();
  }
  
  public static final int IGNORE_FRAMES = 1;
  public static final int IGNORE_LABELS = 1 << 1;
  public static final int IGNORE_LINENUMBERS = 1 << 2;
  public static final int CODE_ONLY = IGNORE_FRAMES | IGNORE_LABELS | IGNORE_LINENUMBERS;
  
  private final int flags;
  private final ImmutableList<Predicate<AbstractInsnNode>> insnPredicates;
  
  private ASMPattern(List<Predicate<AbstractInsnNode>> predicates, int flags) {
    this.insnPredicates = ImmutableList.copyOf(predicates);
    this.flags = flags;
  }
  
  public InsnPattern test(MethodNode main) {
    return test(main.instructions.getFirst());
  }
  
  public InsnPattern test(AbstractInsnNode start) {
    return ASMHelper.findPattern(
      start,
      insnPredicates.size(),
      // isValidNode
      (node) ->
        !testFlag(node, FrameNode.class, IGNORE_FRAMES)
          && !testFlag(node, LabelNode.class, IGNORE_LABELS)
          && !testFlag(node, LineNumberNode.class, IGNORE_LINENUMBERS),
      // nodePredicate
      (found, node) -> insnPredicates.get(found).test(node),
      InsnPattern::new);
  }
  
  // returns true if the node is an instance of the given type and the given flag is present
  private boolean testFlag(
    AbstractInsnNode node, Class<? extends AbstractInsnNode> type, int flag) {
    return type.isInstance(node) && (this.flags & flag) != 0;
  }
  
  public static class Builder {

    private final List<Predicate<AbstractInsnNode>> predicates = new ArrayList<>();
    private int flags;
    
    public Builder() {
      this.flags = 0;
    }

    public Builder ignoreFrames() {
      this.flags |= IGNORE_FRAMES;
      return this;
    }

    public Builder ignoreLabels() {
      this.flags |= IGNORE_LABELS;
      return this;
    }

    public Builder ignoreLineNumbers() {
      this.flags |= IGNORE_LINENUMBERS;
      return this;
    }

    // cringe
    public Builder codeOnly() {
      return ignoreFrames().ignoreLabels().ignoreLineNumbers();
    }
    
    public Builder opcode(int opcode) {
      return add(insn -> insn.getOpcode() == opcode);
    }
    
    public Builder opcodes(int... opcodes) {
      for (int o : opcodes) {
        opcode(o);
      }
      return this;
    }
    
    public Builder invoke() {
      return add(insn -> insn instanceof MethodInsnNode);
    }
    
    public Builder any() {
      return add(insn -> true);
    }
    
    public Builder label() {
      if ((flags & IGNORE_LABELS) != 0) {
        throw new IllegalStateException("Attempting to find a label with flag IGNORE_LABELS");
      }
      return add(insn -> insn instanceof LabelNode);
    }

    public Builder constant(Object value) {
      return add(insn -> insn instanceof LdcInsnNode && ((LdcInsnNode)insn).cst.equals(value));
    }
    
    public <T extends AbstractInsnNode> Builder custom(Predicate<T> predicate) {
      return add(predicate);
    }
    
    private Builder add(Predicate<? extends AbstractInsnNode> predicate) {
      predicates.add((Predicate<AbstractInsnNode>) predicate);
      return this;
    }
    
    public ASMPattern build() {
      return new ASMPattern(predicates, flags);
    }

    public InsnPattern find(AbstractInsnNode node) {
      return build().test(node);
    }

    public InsnPattern find(MethodNode node) {
      return find(node.instructions.getFirst());
    }
  }
}
