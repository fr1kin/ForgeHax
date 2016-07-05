package com.matt.forgehax.asm.helper;

public class AsmMethod extends AsmType<AsmMethod> {
    private AsmMethod[] hooks = new AsmMethod[] {};
    private AsmClass parentClass;
    private Object[] argumentTypes;
    private Object returnType = void.class;

    public AsmMethod setHooks(AsmMethod... hooks) {
        this.hooks = hooks;
        return this;
    }
    public AsmMethod[] getHooks() {
        return this.hooks;
    }

    public AsmMethod setArgumentTypes(Object... types) {
        argumentTypes = types;
        return this;
    }
    public AsmMethod setReturnType(Object type) {
        returnType = type;
        return this;
    }

    public String getDescriptor() {
        StringBuilder builder = new StringBuilder("");
        if(argumentTypes != null) {
            for (Object var : argumentTypes) {
                builder.append(AsmHelper.objectToDescriptor(var));
            }
        }
        return String.format("(%s)%s", builder.toString(), AsmHelper.objectToDescriptor(returnType));
    }

    public AsmMethod setParentClass(AsmClass clazz) {
        this.parentClass = clazz;
        return this;
    }
    public AsmClass getParentClass() {
        return parentClass;
    }

    public String toString() {
        return String.format("%s%s", getName(), getDescriptor());
    }
}
