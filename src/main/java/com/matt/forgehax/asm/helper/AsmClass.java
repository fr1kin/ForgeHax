package com.matt.forgehax.asm.helper;

public class AsmClass extends AsmType<AsmClass> {
    public AsmMethod childMethod() {
        return new AsmMethod().setParentClass(this);
    }

    public AsmField childField() {
        return new AsmField().setParentClass(this);
    }

    public String toString() {
        return getName();
    }
}
