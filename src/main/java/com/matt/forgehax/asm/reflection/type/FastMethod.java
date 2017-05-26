package com.matt.forgehax.asm.reflection.type;

import com.matt.forgehax.asm.helper.AsmStackLogger;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created on 5/25/2017 by fr1kin
 */
public class FastMethod<V> extends FastType<Method> {
    private final Class<?>[] parameters;

    public FastMethod(Class<?> insideClass, String[] names, Class<?>[] parameters) {
        super(insideClass, names);
        this.parameters = Arrays.copyOf(parameters, parameters.length);
    }

    public <E> V invoke(E instance, V defaultValue, Object... args) {
        try {
            if(attemptLookup()) return (V)type.invoke(instance, args);
        } catch (Exception e) {
            if(printOnce.compareAndSet(false, true))
                AsmStackLogger.printStackTrace(e);
        }
        return defaultValue;
    }

    public <E> V invoke(E instance, Object... args) {
        return invoke(instance, null, args);
    }

    @Override
    protected Method lookup() throws Exception {
        Objects.requireNonNull(parameters);
        if(names.length > 0) {
            String mcp = names[0];
            String srg = names.length > 1 ? names[1] : Strings.EMPTY;
            return ReflectionHelper.findMethod(insideClass, mcp, srg, parameters);
        } return null;
    }
}
