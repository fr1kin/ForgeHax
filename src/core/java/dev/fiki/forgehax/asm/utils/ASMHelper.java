package dev.fiki.forgehax.asm.utils;

import dev.fiki.forgehax.asm.utils.asmtype.ASMField;
import dev.fiki.forgehax.asm.utils.asmtype.ASMMethod;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

public class ASMHelper {
  
  /**
   * Finds a pattern of opcodes and returns the first node of the matched pattern if found
   *
   * @param start starting node
   * @param pattern integer array of opcodes
   * @param mask same length as the pattern. 'x' indicates the node will be checked, '?' indicates
   * the node will be skipped over (has a bad opcode)
   * @return top node of matching pattern or null if nothing is found
   */
  public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, char[] mask) {
    if (pattern.length != mask.length) {
      throw new IllegalArgumentException("Mask must be same length as pattern");
    }
    return findPattern(
      start,
      pattern.length,
      (node) -> true,
      (found, next) -> mask[found] != 'x' || next.getOpcode() == pattern[found],
      (first, last) -> first);
  }
  
  public static <T> T findPattern(
    final AbstractInsnNode start,
    final int patternSize,
    Predicate<AbstractInsnNode>
      isValidNode, // if this returns false then dont invoke the predicate and dont update found
    BiPredicate<Integer, AbstractInsnNode> nodePredicate,
    BiFunction<AbstractInsnNode, AbstractInsnNode, T> outputFunction) {
    if (start != null) {
      int found = 0;
      AbstractInsnNode firstFound = null;
      AbstractInsnNode next = start;
      do {
        // Check if node matches the predicate.
        // If the node is not considered a "valid" node then we'll consider it to match the pattern
        // but the predicate will not be invoked for it and found will not be incremented
        final boolean validNode = isValidNode.test(next);
        if (!validNode || nodePredicate.test(found, next)) {
          if (validNode) {
            if(found == 0) firstFound = next;
            // Increment number of matched opcodes
            found++;
          }
        } else {
          // Go back to the starting node
          for (int i = 1; i <= (found - 1); i++) {
            next = next.getPrevious();
          }
          // Reset the number of insns matched
          found = 0;
          firstFound = null;
        }
        
        // Check if found entire pattern
        if (found >= patternSize) {
          return outputFunction.apply(firstFound, next);
        }
        next = next.getNext();
      } while (next != null);
    }
    // failed to find pattern
    return null;
  }
  
  public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, String mask) {
    return findPattern(start, pattern, mask.toCharArray());
  }
  
  public static AbstractInsnNode findPattern(AbstractInsnNode start, int... opcodes) {
    StringBuilder mask = new StringBuilder();
    for (int op : opcodes) {
      mask.append(op == MagicOpcodes.NONE ? '?' : 'x');
    }
    return findPattern(start, opcodes, mask.toString());
  }
  
  public static AbstractInsnNode findPattern(InsnList instructions, int... opcodes) {
    return findPattern(instructions.getFirst(), opcodes);
  }
  
  public static AbstractInsnNode findPattern(MethodNode node, int... opcodes) {
    return findPattern(node.instructions, opcodes);
  }
  
  @Nullable
  public static AbstractInsnNode forward(AbstractInsnNode start, int n) {
    AbstractInsnNode node = start;
    for (int i = 0;
      i < Math.abs(n) && node != null;
      ++i, node = n > 0 ? node.getNext() : node.getPrevious()) {
    }
    return node;
  }
  
  public static MethodInsnNode call(int opcode, boolean isInterface, ASMMethod method) {
    Objects.requireNonNull(method.getParentClass(), "Method requires assigned parent class");
    return new MethodInsnNode(
      opcode,
      method.getParentClass().getName(),
      method.getName(),
      method.getDescriptorString(),
      false);
  }
  
  public static MethodInsnNode call(int opcode, ASMMethod method) {
    return call(opcode, false, method);
  }
  
  public static FieldInsnNode call(int opcode, ASMField field) {
    throw new UnsupportedOperationException("removed type info from ASMField (since mcpbot doesnt provide it)");
//    Objects.requireNonNull(field.getParentClass(), "Field requires assigned parent class");
//    return new FieldInsnNode(
//      opcode,
//      field.getParentClass().getName(),
//      field.getName(),
//      field.getSrgType().getDescriptor());
  }
  
  // scope is from first label to last label
  public static int addNewLocalVariable(MethodNode method, String name, String desc) {
    ASMPattern labelPattern = ASMPattern.builder().build();
    
    final LabelNode start = labelPattern.test(method).getFirst();
    
    // TODO: implement backwards pattern matching so this can be refactored
    AbstractInsnNode iter = method.instructions.getFirst();
    LabelNode end = null;
    do {
      if (iter instanceof LabelNode) {
        end = (LabelNode) iter;
      }
      iter = iter.getNext();
    } while (iter != null);
    if (end == null) {
      throw new IllegalArgumentException("Failed to find LabelNode");
    }
    
    return addNewLocalVariable(method, name, desc, start, end);
  }
  
  public static int addNewLocalVariable(
    MethodNode method, String name, String desc, LabelNode start, LabelNode end) {
    Optional<LocalVariableNode> lastVar =
      method.localVariables.stream().max(Comparator.comparingInt(var -> var.index));
    final int newIndex =
      lastVar.map(var -> var.desc.matches("[JD]") ? var.index + 2 : var.index + 1).orElse(0);
    
    LocalVariableNode variable = new LocalVariableNode(name, desc, null, start, end, newIndex);
    method.localVariables.add(variable);
    
    return newIndex;
  }
  
  // args should be type descriptors
  public static InsnList newInstance(String name, String[] argTypes, @Nullable InsnList args) {
    final String desc = Stream.of(argTypes).collect(Collectors.joining("", "(", ")V"));
    return newInstance(name, desc, args);
  }
  
  public static InsnList newInstance(String name, String desc, @Nullable InsnList args) {
    InsnList list = new InsnList();
    list.add(new TypeInsnNode(NEW, name));
    list.add(new InsnNode(DUP));
    if (args != null) {
      list.add(args);
    }
    list.add(new MethodInsnNode(INVOKESPECIAL, name, "<init>", desc, false));
    return list;
  }
  
  public interface MagicOpcodes {
    
    int NONE = -666;
  }
}
