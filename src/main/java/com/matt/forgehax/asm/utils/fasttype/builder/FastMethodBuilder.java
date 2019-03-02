package com.matt.forgehax.asm.utils.fasttype.builder;

import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.fasttype.FastMethod;
import com.matt.forgehax.asm.utils.name.IName;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Objects;

public class FastMethodBuilder extends FastTypeBuilder<FastMethodBuilder> {
    public static FastMethodBuilder create() {
        return new FastMethodBuilder();
    }

    private Class<?>[] parameters = null;
    private Class<?> returnType = null;

    public FastMethodBuilder setParameters(Class<?>... parameters) {
        this.parameters = Arrays.copyOf(parameters, parameters.length);
        return this;
    }

    /**
     * Only required if you want to use autoAssign() on a method
     *
     * @param returnType
     */
    public FastMethodBuilder setReturnType(Class<?> returnType) {
        this.returnType = returnType;
        return this;
    }

    public <V> FastMethod<V> build() {
        Objects.requireNonNull(insideClass);
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);
        IName<String> iName;
        if (auto && RuntimeState.isSrg()) {
            Objects.requireNonNull(returnType, "Return type required for auto assigning methods");
            String parentClassInternalName = Type.getType(insideClass).getInternalName();
            // build method descriptor
            Type[] args = new Type[parameters.length];
            for (int i = 0; i < args.length; i++) args[i] = Type.getType(parameters[i]);
            String descriptor = Type.getMethodType(Type.getType(returnType), args).getDescriptor();
            srgName = MAPPER.getSrgMethodName(parentClassInternalName, name, descriptor);
            iName = NameBuilder.createMultiName(name, srgName);
        } else {
            iName = NameBuilder.createSingleName(name);
        }
        return new FastMethod<V>(insideClass, iName, parameters);
    }
}
