package com.matt.forgehax.asm.helper;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.F_NEW;

public class AsmHelper {
    public final static Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<Class<?>, Class<?>>();
    public final static Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<Class<?>, Class<?>>();
    public final static Map<Class<?>, Character> primitiveToDescriptor = new HashMap<Class<?>, Character>();

    static {
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(void.class, Void.class);

        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Character.class, char.class);
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Double.class, double.class);
        wrapperToPrimitive.put(Void.class, void.class);

        primitiveToDescriptor.put(boolean.class, 'Z');
        primitiveToDescriptor.put(byte.class, 'B');
        primitiveToDescriptor.put(short.class, 'S');
        primitiveToDescriptor.put(char.class, 'C');
        primitiveToDescriptor.put(int.class, 'I');
        primitiveToDescriptor.put(long.class, 'J');
        primitiveToDescriptor.put(float.class, 'F');
        primitiveToDescriptor.put(double.class, 'D');
        primitiveToDescriptor.put(void.class, 'V');
    }

    /**
     * Prints all the opcodes from the start of the node to the end of the node
     * @param start starting point
     */
    public static void outputBytecode(AbstractInsnNode start) throws Exception {
        StringBuilder hexPattern = new StringBuilder(), mask = new StringBuilder(), deciPattern = new StringBuilder();
        AbstractInsnNode next = start;
        do {
            hexPattern.append("0x");
            if(next.getOpcode() != F_NEW) {
                mask.append("x");
                hexPattern.append(Integer.toHexString(next.getOpcode()));
                deciPattern.append(String.format("%4d", next.getOpcode()));
            } else {
                mask.append("?");
                hexPattern.append("00");
                deciPattern.append("NULL");
            }
            hexPattern.append("\\");
            deciPattern.append(", ");
            next = next.getNext();
        } while(next != null);
        throw new Exception("Pattern (base10): " + deciPattern.toString() + "\n" +
                "Pattern (base16): " + hexPattern.toString() + "\n" +
                "Mask: " + mask.toString() + "\n");
    }

    /**
     * Breaks a string of hexadecimal opcodes into a base10 integer array
     * @param pattern pattern
     * @return Integer array
     */
    public static int[] convertPattern(String pattern) {
        if(pattern.startsWith("\0"))
            pattern = pattern.substring(1);
        String[] hex = pattern.split("\0");
        int[] buff = new int[hex.length];
        int index = 0;
        for(String number : hex) {
            if(number.startsWith("0x"))
                number = number.substring(2);
            else if(number.startsWith("x"))
                number = number.substring(1);
            buff[index++] = Integer.parseInt(number, 16);
        }
        return buff;
    }

    /**
     * Finds a pattern of opcodes and returns the first node of the matched pattern if found
     * @param start starting node
     * @param pattern integer array of opcodes
     * @param mask same length as the pattern. 'x' indicates the node will be checked, '?' indicates the node will be skipped over (has a bad opcode)
     * @return top node of matching pattern or null if nothing is found
     */
    public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, char[] mask) throws Exception {
        if(start != null &&
                pattern.length == mask.length) {
            int found = 0;
            AbstractInsnNode next = start;
            do {
                switch(mask[found]) {
                    // Analyze this node
                    case 'x': {
                        // Check if node and pattern have same opcode
                        if(next.getOpcode() == pattern[found]) {
                            // Increment number of matched opcodes
                            found++;
                        } else {
                            // Go back to the starting node
                            for(int i = 1; i <= (found - 1); i++) {
                                next = next.getPrevious();
                            }
                            // Reset the number of opcodes found
                            found = 0;
                        }
                        break;
                    }
                    // Skips over this node
                    default:
                    case '?':
                        found++;
                        break;
                }
                // Check if found entire pattern
                if(found >= mask.length) {
                    // Go back to top node
                    for(int i = 1; i <= (found - 1); i++)
                        next = next.getPrevious();
                    return next;
                }
                next = next.getNext();
            } while(next != null &&
                    found < mask.length);
        }
        throw new Exception("Failed to match pattern");
    }
    public static AbstractInsnNode findPattern(AbstractInsnNode start, String pattern, String mask) throws Exception {
        return findPattern(start,
                convertPattern(pattern),
                mask.toCharArray());
    }
    public static AbstractInsnNode findPattern(AbstractInsnNode start, int[] pattern, String mask) throws Exception {
        return findPattern(start,
                pattern,
                mask.toCharArray());
    }

    public static AbstractInsnNode findStart(AbstractInsnNode start) {
        AbstractInsnNode next = start;
        do {
            if(next.getOpcode() != -1)
                return next;
            else
                next = next.getNext();
        } while(next != null);
        return null;
    }

    public static String getClassData(ClassNode node) {
        StringBuilder builder = new StringBuilder("METHODS:\n");
        for(MethodNode method : node.methods) {
            builder.append("\t");
            builder.append(method.name);
            builder.append(method.desc);
            builder.append("\n");
        }
        builder.append("\nFIELDS:\n");
        for(FieldNode field : node.fields) {
            builder.append("\t");
            builder.append(field.desc);
            builder.append(" ");
            builder.append(field.name);
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String objectToDescriptor(Object obj) {
        if (obj instanceof String)
            return (String)obj;
        else if (obj instanceof AsmClass) {
            AsmClass clazz = (AsmClass)obj;
            return String.format("L%s;", clazz.getRuntimeName());
        } else if(obj instanceof Class) {
            return Type.getDescriptor((Class)obj);
        } else return "";
    }
}
