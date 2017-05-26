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
    // {mcp, srg, obf}
    private final String[] names = new String[] {Strings.EMPTY, Strings.EMPTY, Strings.EMPTY};
    private Class<?>[] parameters = null;

    public FastMethod(Class<?> insideClass) {
        super(insideClass);
    }

    public FastMethod<V> mcpName(String name) {
        names[0] = name;
        return this;
    }

    public FastMethod<V> srgName(String name) {
        names[1] = name;
        return this;
    }

    public FastMethod<V> obfName(String name) {
        names[2] = name;
        return this;
    }

    public void withParameters(Class<?>... parameters) {
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
        String mcp = names[0];
        String srg = !Strings.isNullOrEmpty(names[1]) ? names[1] : names[2];
        return ReflectionHelper.findMethod(insideClass, mcp, srg, parameters);
    }
}
