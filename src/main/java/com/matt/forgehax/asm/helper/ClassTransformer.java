package com.matt.forgehax.asm.helper;

import com.google.common.collect.Lists;
import com.matt.forgehax.asm.Names;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public abstract class ClassTransformer {
    protected final Names NAMES = Names.INSTANCE;

    public static final int CLASS_NAME_POS = 0;
    public static final int CLASS_HOOK_POS = 1;

    private List<String> errorLog = Lists.newArrayList("", "");

    public int methodCount = 0;
    public int foundMethods = 0;

    private boolean allMethodsPatched = true;

    public void setClassName(String realName, String runName) {
        errorLog.set(CLASS_NAME_POS, String.format("[ClassNames]: %s, %s\n", realName, runName));
        errorLog.set(CLASS_HOOK_POS, String.format("%-100s | %-100s\n", "Targets", "Hooks"));
    }

    protected void registerHook(AsmMethod method) {
        methodCount++;
        StringBuilder builder = new StringBuilder("");
        for(AsmMethod hook : method.getHooks()) {
            builder.append(hook.getName());
            builder.append(",");
        }
        errorLog.add(CLASS_HOOK_POS + 1, String.format("%-100s | %-100s\n",
                String.format("%s, %s :: %s", method.getName(), method.getObfuscatedName(), method.getDescriptor()),
                builder.toString()
        ));
    }

    public void transform(ClassNode node) {
        //log("ClassData", "%s\n", AsmHelper.getClassData(node));
        for(MethodNode method : node.methods) {
            if(hasFoundAllMethods()) break; // stop searching after we have found all the methods
            if(onTransformMethod(method))
                foundMethods++;
        }
    }

    protected void updatePatchedMethods(boolean result) {
        allMethodsPatched = allMethodsPatched && result;
        if(!result) log("updatePatchedMethods", "Failed to find method");
    }

    public void markUnsuccessful() {
        allMethodsPatched = false;
    }

    public List<String> getErrorLog() {
        return errorLog;
    }

    public boolean hasFoundAllMethods() {
        return foundMethods == methodCount;
    }

    public boolean isSuccessful() {
        return allMethodsPatched && hasFoundAllMethods();
    }

    public AbstractInsnNode findPattern(String methodName, String nodeName, AbstractInsnNode start, int[] pattern, String mask) {
        AbstractInsnNode node = null;
        try {
            node = AsmHelper.findPattern(start, pattern, mask);
        } catch (Exception e) {
            log(methodName, nodeName + " error: %s\n", e.getMessage());
        }
        return node;
    }

    public void log(String methodName, String text, Object... args) {
        errorLog.add(String.format(String.format("[%s]: %s", methodName, text), args));
    }

    public abstract boolean onTransformMethod(MethodNode method);
}
