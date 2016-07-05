package com.matt.forgehax.asm.helper;

public class AsmField extends AsmType<AsmField> {
    private AsmClass parentClass;
    private Object type;

    public String getTypeDescriptor() {
        return AsmHelper.objectToDescriptor(type);
    }
    public AsmField setType(Object o) {
        type = o;
        return this;
    }

    public AsmClass getParentClass() {
        return parentClass;
    }
    public AsmField setParentClass(AsmClass parentClass) {
        this.parentClass = parentClass;
        return this;
    }

    public String toString() {
        return String.format("%s %s", getTypeDescriptor(), getName());
    }
}
