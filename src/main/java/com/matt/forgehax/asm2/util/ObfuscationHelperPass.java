package com.matt.forgehax.asm2.util;

import com.fr1kin.asmhelper.types.ASMMethod;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.Map;

/**
 * Created on 1/15/2017 by fr1kin
 */
public class ObfuscationHelperPass extends ObfuscationHelper {
    protected ObfuscationHelperPass(Logger logger) throws IOException {
        super(logger);
    }

    @Override
    protected void startupMsg() {
        getLogger().info("initializing ObfuscationHelper WITHOUT obfuscation");
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    protected String getClassName(String className, Map<String, String> map) {
        return className;
    }

    @Override
    public String getObfMethodName(String parentClassName, String methodName, String methodDescriptor) {
        return methodName;
    }

    @Override
    public String getObfMethodName(ASMMethod method) {
        return method.getName();
    }

    @Override
    public String getObfFieldName(String parentClassName, String fieldName) {
        return fieldName;
    }

    @Override
    public Type translateMethodType(Type methodType) {
        return methodType;
    }

    @Override
    public Type translateFieldType(Type fieldType) {
        return fieldType;
    }
}
