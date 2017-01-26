package com.matt.forgehax.asm2.types;

import com.fr1kin.asmhelper.ASMHelper;
import com.fr1kin.asmhelper.types.ASMClass;
import com.fr1kin.asmhelper.types.ASMField;
import com.fr1kin.asmhelper.types.ASMMethod;
import com.matt.forgehax.asm2.CoreMod;
import com.matt.forgehax.asm2.util.ObfuscationHelper;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created on 1/14/2017 by fr1kin
 */
public class ASMObfClass extends ASMClass {
    public static ASMClass getOrCreateClass(final Type type) {
        Type obfType = Type.getObjectType(CoreMod.getObfuscationHelper().getObfClassName(type.getInternalName()));
        return ASM_CLASS_CACHE.computeIfAbsent(obfType, key -> new ASMObfClass(obfType, type));
    }

    public static ASMClass getOrCreateClass(String classDescriptor) {
        return getOrCreateClass(ASMHelper.getInternalClassType(classDescriptor));
    }

    public static ASMClass getOrCreateClass(ClassNode classNode) {
        return getOrCreateClass(classNode.signature);
    }

    private final Type mcpType;

    private ASMObfClass(Type type, Type mcpType) {
        super(type);
        this.mcpType = mcpType;
    }

    public ASMClass getMcpClass() {
        return ASMClass.getOrCreateClass(mcpType);
    }

    @Override
    public ASMMethod childMethod(String name, boolean isStatic, Type methodType) {
        ObfuscationHelper obfHelper = CoreMod.getObfuscationHelper();
        return new ASMMethod(
                obfHelper.getObfMethodName(
                        getMcpClass().getName(),
                        name,
                        obfHelper.translateMethodType(methodType).getDescriptor()
                ), this, isStatic, methodType
        );
    }

    @Override
    public ASMField childField(String name, boolean isStatic, Type type) {
        ObfuscationHelper obfHelper = CoreMod.getObfuscationHelper();
        return new ASMField(
                obfHelper.getObfFieldName(
                        getMcpClass().getName(),
                        name
                ), this, isStatic, type
        );
    }
}
