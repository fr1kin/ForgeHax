package com.matt.forgehax.asm.utils.remapping;

import com.matt.forgehax.asm.utils.environment.IStateMapper;

import javax.annotation.Nullable;

public class SrgStateMapper implements IStateMapper {
    private static SrgStateMapper INSTANCE = new SrgStateMapper();

    public static SrgStateMapper getInstance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public String getSrgMethodName(String parentClassName, String methodName, String methodDescriptor) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Nullable
    @Override
    public String getSrgFieldName(String parentClassName, String fieldName) {
        throw new UnsupportedOperationException("not implemented");
    }
}
